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
    private int piece_size = 4096 ;
    private int firstPieceId;
    private Metadata metadata;


    public Worker(int rangeToRead, int offset, int serialNumber,String url_str,BlockingQueue<DataPiece> blocking_queue, int firstPieceId, Metadata metadata){

        this.rangeToRead = rangeToRead;
        this.offset = offset;
        this.serialNumber = serialNumber;
        this.url_str = url_str;
        this.queue = blocking_queue;
        this.firstPieceId = firstPieceId;
        this.metadata = metadata;

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
            readContent(connection, this.rangeToRead, this.piece_size, this.offset, this.queue, this.firstPieceId, this.metadata);

            System.out.println("queue size:" + queue.size());

        } catch (Exception e) {
            System.err.println("HTTP request failed " + e.getMessage() + ",Download failed");
            return;
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public static void readContent(HttpURLConnection connection, int rangeToRead, int piece_size, int offset, BlockingQueue<DataPiece> queue, int firstPieceId, Metadata metadata) throws IOException {

        System.out.println("read content was called");

        InputStream in = null;
        ArrayList<Integer> content = new ArrayList<>();
        try {
            in = connection.getInputStream();
            int inputRead = 0;
            while (inputRead < rangeToRead){
                //check if this is the last thread
                if((rangeToRead % piece_size) != 0){
                    if ((rangeToRead - inputRead) < piece_size) {
                        piece_size = rangeToRead - inputRead; //only the last piece of the last thread will change size
                    }
                }

//                System.out.println("piece size:" + piece_size);

                int currrent_byte;
                byte[] input_piece = new byte[piece_size];
                for(int i = 0; i < piece_size; i++){
                    if((currrent_byte = in.read()) == -1) break;
                    input_piece[i] = (byte) currrent_byte;
                }
                DataPiece current_piece = new DataPiece(offset, input_piece, piece_size, firstPieceId);
                queue.add(current_piece);
                inputRead += piece_size;
                offset += piece_size;
                firstPieceId++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(in != null)
                in.close();
        }
    }
}
