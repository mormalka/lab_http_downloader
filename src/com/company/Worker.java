package com.company;

import java.net.HttpURLConnection;
import java.net.URL;

public class Worker implements Runnable{
    private int rangeToRead;
    private int offset;
    private int serialNumber;
    private String url_str;

    @Override
    public void run() {
//        HttpURLConnection connection = null;
//        //Creates the connection
//        try {
//            URL url = new URL(url_str);
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("HEAD"); //Send request
//
//            //Assume the server knows the content length
//            FILE_LEN = connection.getContentLength();
//            System.out.println("file length " + FILE_LEN);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return;
//        }finally {
//            if (connection != null) {
//                connection.disconnect();
//            }
//        }

    }

    public Worker(int rangeToRead, int offset, int serialNumber,String url_str){

        this.rangeToRead = rangeToRead;
        this.offset = offset;
        this.serialNumber = serialNumber;
        this.url_str = url_str;

        System.out.println(" rangeToRead- " + rangeToRead + " offset- " +  offset + " serialNumber- " + serialNumber + "  url_str- " +  url_str);
    }
}
