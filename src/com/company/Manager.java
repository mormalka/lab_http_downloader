package com.company;


import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Manager {

    public int file_len = 0;
    public int threads_connections = 0;
    public List<String> url_list;
    public Thread[] threads;
    public BlockingQueue<DataPiece> content_queue = new LinkedBlockingQueue<>();
    public Thread writer;
    public int piece_size = 8192; // Splitting the download into pieces
    public int num_total_pieces;
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

        } catch (MalformedURLException e) {
            System.err.println("Incorrect URL. Download failed.");
            return;
        } catch (ProtocolException ie) {
            System.err.println("Failed to connect server. Download failed.");
            this.handleErrors();
        } catch (IOException ie) {
            System.err.println("Failed to connect server. Download failed.");
            this.handleErrors();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void setNumOfConnection(int n){
        threads_connections = n;
    }

    public void setUrlList(List<String> urls){
        url_list = urls;
    }

    public void initWorkers(){
        int totalPieces = this.file_len / piece_size;
        int workerPieces = totalPieces / threads_connections;
        int rangeToRead = piece_size *workerPieces ;
        this.threads = new Thread[threads_connections];
        num_total_pieces = totalPieces;
        if((this.file_len % piece_size) != 0) num_total_pieces++;

        initMetadata(num_total_pieces);

        int offset = 0;
        int i;
        String[] urls = distributeUrl();

        int firstPieceId = 0; // for pieces id (for metadata array)
        for (i = 0; i < threads_connections -1; i++){
            Worker worker = new Worker(rangeToRead,offset,urls[i], this.content_queue, firstPieceId, this.metadata, piece_size, this);
            this.threads[i] = new Thread(worker);
            offset += rangeToRead; //start point of the next thread to read from file
            firstPieceId += workerPieces;
        }
        // last worker will read the reminder of the file
        Worker worker = new Worker((totalPieces % threads_connections)* piece_size + rangeToRead + (this.file_len % piece_size) ,offset,urls[i],this.content_queue, firstPieceId, this.metadata , piece_size, this);
        this.threads[i] = new Thread(worker);
    }

    public String[] distributeUrl(){
        //each url index respectively to thread serial number
        String[] urls = new String[threads_connections];
        int j = 0;
        for(int i = 0; i<threads_connections; i++){
            urls[i] = url_list.get(j % url_list.size());
            j++;
        }

        return urls;
    }

    public void startWorkers(){
        for(int i = 0 ; i <this.threads.length ; i++){
            this.threads[i].start();
        }
    }

    public void startWriter(){
        File destFile = createDestFile(url_list.get(0));
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
            return file;
        }

        try {
            file.createNewFile();
        } catch (IOException e){
            System.err.println("Creating new file to download into failed. Download failed");
            System.exit(1);
        }

        return file;
    }

    public static String getDownloadFileName(String url) {
        String[] splittedUrl = url.split("/");
        String name = splittedUrl[splittedUrl.length - 1];
        return name;
    }

    public void initMetadata(int total_pieces) {
        String downloadedFileName = getDownloadFileName(url_list.get(0));
        this.metadata = new Metadata(total_pieces, downloadedFileName);
    }

    public void handleErrors(){
        System.exit(1); //didn't finish successfully - status 1
    }
}
