package com.company;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;


public class Worker implements Runnable{
    private int rangeToRead;
    private int offset;
    private int serialNumber;
    private String url_str;
    private BlockingQueue<DataPiece> queue;
    //private int chunk_size = 1000000 ;


    public Worker(int rangeToRead, int offset, int serialNumber,String url_str,BlockingQueue<DataPiece> blocking_queue){

        this.rangeToRead = rangeToRead;
        this.offset = offset;
        this.serialNumber = serialNumber;
        this.url_str = url_str;
        this.queue = blocking_queue;

        System.out.println(" rangeToRead- " + rangeToRead + " offset- " +  offset + " serialNumber- " + serialNumber + "  url_str- " +  url_str);
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        //Creates the connection
        try {
            URL url = new URL(this.url_str);
            connection = (HttpURLConnection) url.openConnection();
            //Create a string which defines the range value
            String range_value = "bytes=" + this.offset + "-" + (this.offset + this.rangeToRead);

            //System.out.println("range value " + range_value);

            connection.setRequestProperty("Range", range_value); //set the range value in the header

            connection.setRequestMethod("GET"); //Firing the GET request
            //System.out.println(connection.getHeaderField("Content-Range"));

            //getting the content of file receives from the request (in the defined range)
            ArrayList<Integer> content = readContent(connection);

            DataPiece piece = new DataPiece(this.offset, content, this.rangeToRead);
            this.queue.add(piece);
            //System.out.println(this.serialNumber + " - " + content.size());
            //System.out.println(queue.size());

        } catch (Exception e) {
            System.err.println("HTTP request failed " + e.getMessage() + ",Download failed");
            return;
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public static ArrayList<Integer> readContent(HttpURLConnection connection) throws IOException {

        InputStreamReader in = null;
        ArrayList<Integer> content = new ArrayList<>();
        try {
            in = new InputStreamReader (connection.getInputStream());

            int currrent_byte;
            while ((currrent_byte = in.read()) != -1){
                content.add(currrent_byte);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in != null)
                in.close();
        }
        return content;

    }
}
