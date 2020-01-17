package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args){

        int thread_Connections = 1; //default single connection thread

        if (args.length < 1 || args.length > 2){
            System.err.println("java IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]");
            // return;
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

        if(file.exists()){ //handle list of urls
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                System.err.println("------------");
            }
            String str = "";
            while(true) {
                try {
                    if (!((str = br.readLine()) != null)) break;
                } catch (IOException e) {
                    System.err.println("------------");
                } //insert urls into list
                url_list.add(str);
                System.out.println("current url " + str);
            }
            //Every server has the same file, in particular the first
            manager.setFileLength(url_list.get(0));
            manager.setUrlList(url_list);

        } else{ //handle single url
            String url_str = args[0];
            //send a HEAD request to the given url and set the file length
            manager.setFileLength(url_str);
            url_list.add(url_str);
            manager.setUrlList(url_list);
        }

        //sets the number of possible threads connections
        manager.setNumOfConnection(thread_Connections);
        manager.initWorkers();
        manager.startWriter();
        manager.startWorkers();


    }

}