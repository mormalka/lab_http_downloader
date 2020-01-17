package com.company;


import java.io.DataOutputStream;

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

        int offset = 0;
        int i;

        String[] urls = distributeUrl();

        for (i = 0; i < THREAD_CONNECTIONS -1; i++){
            Worker worker = new Worker(rangeToRead,offset,i,urls[i],CONTENT_QUEUE);
            THREADS[i] = new Thread(worker);
            offset += rangeToRead; //start point of the next thread to read from file
        }
        // last worker will read the reminder of the file
        Worker worker = new Worker((totalPieces % THREAD_CONNECTIONS)*PIECE_SIZE + rangeToRead ,offset,i,urls[i],CONTENT_QUEUE);
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

        WRITER =new Thread(new Writer(CONTENT_QUEUE));


    }
}
