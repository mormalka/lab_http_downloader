package com.company;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;


public class Worker implements Runnable{
    private int rangeToRead;
    private int offset;
    private int id;
    private String url_str;
    private BlockingQueue<DataPiece> queue;
    private int firstPieceId;
    private Metadata metadata;
    private int piece_size;
    private Manager manager;


    public Worker(int rangeToRead, int offset,String url_str,BlockingQueue<DataPiece> blocking_queue, int firstPieceId, Metadata metadata, int piece_size, Manager manager){

        this.rangeToRead = rangeToRead;
        this.offset = offset;
        this.url_str = url_str;
        this.queue = blocking_queue;
        this.firstPieceId = firstPieceId;
        this.metadata = metadata;
        this.piece_size = piece_size;
        this.manager = manager;

//        System.out.println(" rangeToRead- " + rangeToRead + " offset- " +  offset + " serialNumber- " + id + "  url_str- " +  url_str);
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        //Creates the connection
        try {
            this.id = (int) (Thread.currentThread().getId()); ////////////MOR
            System.err.println("[" + this.id + "] Start downloading range (" + this.offset + " - " + (this.offset + this.rangeToRead) + ") from:\n" + this.url_str);

            URL url = new URL(this.url_str);
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(90000); // waits maximum for a minute and a half to available data to read from connection
            //Create a string which defines the range value
            String range_value = "bytes=" + this.offset + "-" + (this.offset + this.rangeToRead);

            connection.setRequestProperty("Range", range_value); //set the range value in the header
            connection.setRequestMethod("GET"); //Firing the GET request

            //getting the content of file receives from the request (in the defined range)
            this.readContent(connection);


        } catch (MalformedURLException e) {
            System.err.println("Incorrect URL. Download failed.");
            return;

//        } catch (ProtocolException e) { //INTELLIJE SUGGESTED
//            System.err.println("HTTP request failed " + e.getMessage() + "Download failed");
//            manager.handleErrors(e);

        } catch (IOException e) {
            System.err.println("HTTP request failed " + e.getMessage() + "Download failed");
            manager.handleErrors(e);

        }  finally {
            if (connection != null) {
                System.err.println("[" + this.id + "] Finished downloading");
                connection.disconnect();
            }
        }

    }

    public void readContent(HttpURLConnection connection) throws IOException {
        InputStream in = null;
        try {
            in = connection.getInputStream();
            int inputRead = 0;
            while (inputRead < this.rangeToRead){
                //check if this is the last thread
                if((this.rangeToRead % this.piece_size) != 0){
                    if ((this.rangeToRead - inputRead) < this.piece_size) {
                        this.piece_size = this.rangeToRead - inputRead; //only the last piece of the last thread will change size
                    }
                }
                // check if piece allready transfered to writer according to metadata - in case of resume download
                if(!(this.metadata.pieceMap.bitmap[this.firstPieceId])) {
                    int currrent_byte;
                    byte[] input_piece = new byte[piece_size];
                    for (int i = 0; i < this.piece_size; i++) {
                        if ((currrent_byte = in.read()) == -1) break;
                        input_piece[i] = (byte) currrent_byte;
                    }
                    DataPiece current_piece = new DataPiece(this.offset, input_piece, this.piece_size, this.firstPieceId);
                    this.queue.add(current_piece);
                }else{
                    in.skip(this.piece_size);
                }

                inputRead += this.piece_size;
                this.offset += this.piece_size;
                this.firstPieceId++;
            }

        } catch (IOException e) {
            //calling manager to handle errors
            System.err.println("IO Exception while reading content " + e + " Download failed, problem server:" + this.url_str);
            this.manager.handleErrors(e);
        } finally {
            if(in != null)
                in.close();
        }
    }
}
