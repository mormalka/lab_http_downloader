package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IdcDm {

    public static void main(String[] args){

        Manager manager = new Manager();
        int thread_Connections = 1; //default single connection thread

        if (args.length < 1 || args.length > 2){
            System.err.println("usage: java IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]");
            return;
        }
        if (args.length > 1){ //get the number of thread connections given by the user
            int connectionsFromUser =  Integer.parseInt(args[1]);
            if (connectionsFromUser > 1){
                thread_Connections = connectionsFromUser;
            }
        }

        File file = new File(args[0]);
        //initial empty url list
        List<String> url_list = new ArrayList<>();

        if (file.exists()){ //handle list of urls
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.err.println("URLs list file didn't found." +e.getMessage() + ". Download failed");
                System.exit(1);
            }
            String str = "";
            while(true) {
                try {
                    if (!((str = br.readLine()) != null)) break;
                } catch (IOException e) {
                    System.err.println("Failed to read URLs list file." +e.getMessage() + ". Download failed");
                    System.exit(1);
                } //insert urls into list
                url_list.add(str);
            }
            //Every server has the same file, in particular the first
            manager.setFileLength(url_list.get(0));
            manager.setUrlList(url_list);

        } else { //handle single url
            String url_str = args[0];
            //send a HEAD request to the given url and set the file length
            manager.setFileLength(url_str);
            url_list.add(url_str);
            manager.setUrlList(url_list);
        }

        //sets the number of possible threads connections
        manager.setNumOfConnection(thread_Connections);
        //initialize threads to read from destination
        manager.initWorkers();
        //start single thread to write file to disk
        manager.startWriter();
        //start threads to download file
        manager.startWorkers();
    }

}