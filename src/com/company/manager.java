package com.company;


import java.io.DataOutputStream;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class manager {

    public static int FILE_LEN = 0;
    public static int THREAD_CONNECTIONS = 0;
    public static List<String> URL_LIST;
    public static Thread[] THREADS;
    public static BlockingQueue<DataPiece> CONTENT_QUEUE = new LinkedBlockingQueue<>();
    public static Thread  WRITER;
    public static int PIECE_SIZE = 4096;
    public static int NUM_TOTAL_PIRECES;
    public static Metadata METADATA;

    public static void setFileLength (String url_str){
        HttpURLConnection connection = null;
        //Creates the connection
        try {
            URL url = new URL(url_str);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); //Send request

            //Assume the server knows the content length
            FILE_LEN = connection.getContentLength();
            System.out.println("file length " + FILE_LEN);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void setNumOfConnection(int n){
        //TODO
        THREAD_CONNECTIONS = n;
    }

    public static void setUrlList(List<String> urls){
        URL_LIST = urls;
    }

    public static void initWorkers(){
        int totalPieces = FILE_LEN / PIECE_SIZE;
        int workerPieces = totalPieces / THREAD_CONNECTIONS;
        int rangeToRead = PIECE_SIZE*workerPieces ;

        THREADS = new Thread[THREAD_CONNECTIONS];

        NUM_TOTAL_PIRECES = totalPieces +((((totalPieces % THREAD_CONNECTIONS)*PIECE_SIZE) + (FILE_LEN % PIECE_SIZE)) / PIECE_SIZE)  ; // the last one is going to read all the reminder, so check how many times piece size get init
        if (((((totalPieces % THREAD_CONNECTIONS)*PIECE_SIZE) + (FILE_LEN % PIECE_SIZE)) % PIECE_SIZE) != 0){
            NUM_TOTAL_PIRECES++;
        }
        System.out.println("NUM_TOTAL_PIRECES :" + NUM_TOTAL_PIRECES);

        initMetadata(NUM_TOTAL_PIRECES);

        int offset = 0;
        int i;

        String[] urls = distributeUrl();

        int firstPieceId = 0; // for pieces id (for metadata array)
        for (i = 0; i < THREAD_CONNECTIONS -1; i++){
            Worker worker = new Worker(rangeToRead,offset,i,urls[i],CONTENT_QUEUE, firstPieceId, METADATA);
            THREADS[i] = new Thread(worker);
            offset += rangeToRead; //start point of the next thread to read from file
            firstPieceId += workerPieces;
        }
        // last worker will read the reminder of the file
        Worker worker = new Worker((totalPieces % THREAD_CONNECTIONS)*PIECE_SIZE + rangeToRead + (FILE_LEN % PIECE_SIZE) ,offset,i,urls[i],CONTENT_QUEUE, firstPieceId, METADATA);
        THREADS[i] = new Thread(worker);
    }



    public static String[] distributeUrl(){
        //each url index respectively to thread serial number
        String[] urls = new String[THREAD_CONNECTIONS];
        int j = 0;
        for(int i = 0; i<THREAD_CONNECTIONS; i++){
            urls[i] = URL_LIST.get(j % URL_LIST.size());
            j++;
        }

        return urls;
    }

    public static void startWorkers(){
        for(int i = 0 ; i <THREADS.length ; i++){
            THREADS[i].start();
        }
    }

    public static void startWriter(){
        File destFile = createDestFile(URL_LIST.get(0));
        WRITER = new Thread(new Writer(CONTENT_QUEUE, FILE_LEN, destFile, METADATA));
        WRITER.start();

    }

    public static File createDestFile(String url){
        String[] splittedUrl = url.split("/");
        String name = splittedUrl[splittedUrl.length - 1];
        String path = "C:\\Test\\" + name; // CHANGE TO CURRENT
        File file = new File(path);

        try {
            file.createNewFile();
        } catch (IOException e){
            System.err.println("Creating new file failed " + e.getMessage() + ",Download failed");
            //return ?????
        }

        return file;
    }

    public static void initMetadata(int NUM_TOTAL_PIRECES){
        METADATA = new Metadata(NUM_TOTAL_PIRECES);
    }
}
