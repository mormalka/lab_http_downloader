package com.company;


import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Manager {

    public int file_len = 0;
    public int THREAD_CONNECTIONS = 0;
    public List<String> URL_LIST;
    public Thread[] threads;
    public BlockingQueue<DataPiece> content_queue = new LinkedBlockingQueue<>();
    public Thread writer;
    public int PIECE_SIZE = 8192; // Splitting the download into pieces
    public int NUM_TOTAL_PIECES;
    public Metadata metadata;


    public void setFileLength (String url_str){
        HttpURLConnection connection = null;
        //Creates the connection
        try {
            URL url = new URL(url_str);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); //Send request

            //Assume the server knows the content length
            this.file_len = connection.getContentLength();
            System.out.println("file length " + this.file_len); //REMOVE

        } catch (MalformedURLException e){
            System.err.println("Incorrect URL. Download failed.");

        } catch (IOException ie) {
            System.err.println("Failed to connect " + ie.getMessage() + " Download faild.");
            this.handleErrors(ie);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void setNumOfConnection(int n){
        //TODO
        THREAD_CONNECTIONS = n;
    }

    public void setUrlList(List<String> urls){
        URL_LIST = urls;
    }

    public void initWorkers(){
        int totalPieces = this.file_len / PIECE_SIZE;
        int workerPieces = totalPieces / THREAD_CONNECTIONS;
        int rangeToRead = PIECE_SIZE*workerPieces ;

        this.threads = new Thread[THREAD_CONNECTIONS];

        NUM_TOTAL_PIECES = totalPieces;
        if((this.file_len % PIECE_SIZE) != 0) NUM_TOTAL_PIECES++;

        initMetadata(NUM_TOTAL_PIECES);

        int offset = 0;
        int i;

        String[] urls = distributeUrl();

        int firstPieceId = 0; // for pieces id (for metadata array)
        for (i = 0; i < THREAD_CONNECTIONS -1; i++){
            Worker worker = new Worker(rangeToRead,offset,urls[i], this.content_queue, firstPieceId, this.metadata, PIECE_SIZE, this);
            this.threads[i] = new Thread(worker);
            offset += rangeToRead; //start point of the next thread to read from file
            firstPieceId += workerPieces;
        }
        // last worker will read the reminder of the file
        Worker worker = new Worker((totalPieces % THREAD_CONNECTIONS)*PIECE_SIZE + rangeToRead + (this.file_len % PIECE_SIZE) ,offset,urls[i],this.content_queue, firstPieceId, this.metadata ,PIECE_SIZE, this);
        this.threads[i] = new Thread(worker);
    }



    public String[] distributeUrl(){
        //each url index respectively to thread serial number
        String[] urls = new String[THREAD_CONNECTIONS];
        int j = 0;
        for(int i = 0; i<THREAD_CONNECTIONS; i++){
            urls[i] = URL_LIST.get(j % URL_LIST.size());
            j++;
        }

        return urls;
    }

    public void startWorkers(){
//        System.err.println("Downloading...");
        for(int i = 0 ; i <this.threads.length ; i++){
            this.threads[i].start();
        }
    }

    public void startWriter(){
        File destFile = createDestFile(URL_LIST.get(0));
        this.writer = new Thread(new Writer(this.content_queue, this.file_len, destFile, this.metadata, this));
        this.writer.start();
    }

    public static File createDestFile(String url){
        String name = getDownloadFileName(url);
        String currentDir = new File("").getAbsolutePath();
        String path = currentDir + "/" + name;
        File file = new File(path);
        // in case of resume
        if(file.exists()){
            System.out.println("$$$$$$$ file exist");
            return file;
        }

        try {
            file.createNewFile();
        } catch (IOException e){
            System.err.println("Creating new file failed " + e.getMessage() + ", Download failed");
        }

        return file;
    }

    public static String getDownloadFileName(String url) {
        String[] splittedUrl = url.split("/");
        String name = splittedUrl[splittedUrl.length - 1];
        return name;
    }

    public void initMetadata(int NUM_TOTAL_PIECES)
    {
        String downloadedFileName = getDownloadFileName(URL_LIST.get(0));
        this.metadata = new Metadata(NUM_TOTAL_PIECES, downloadedFileName);
    }

    public void handleErrors(Exception e){
        System.exit(1); //didn't finish successfully - status 1
    }
}
