package com.company;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

public class Writer implements Runnable {

    public BlockingQueue<DataPiece>queue;
    public int file_len;
    public int numOfReadBytes; //num of bytes that the writer already read
    public File dest_file;
    public Metadata metadata;
    public boolean isWriterFinished = false;
    public int downloadPrecentage;
    public Manager manager;

    public Writer (BlockingQueue<DataPiece> queue, int file_len, File file, Metadata metadata, Manager manager){
        this.queue = queue;
        this.file_len = file_len;
        this.dest_file = file;
        this.metadata = metadata;
        this.numOfReadBytes = 0;
        this.downloadPrecentage = getPercentageCompleted();
        this.manager = manager;
    }


    @Override
    public void run() {
        try {
            if(metadata.isFirstRun){
                System.err.println("Downloaded 0%");
            }
            RandomAccessFile randomAccess = new RandomAccessFile(dest_file, "rw");
            while (this.downloadPrecentage < 100) {
                if (!this.queue.isEmpty()) {
                    DataPiece dataPiece = queue.poll();
                    randomAccess.seek(dataPiece.offset);
                    randomAccess.write(dataPiece.content);
                    this.metadata.approvePiece(dataPiece.id);
                    numOfReadBytes += dataPiece.size;
                    printAndUpdatePercentageCompleted();
                }

            }
            System.err.println("Metadata file size: " + this.metadata.metadata_file.length()); // REMOVE

            this.metadata.metadata_file.delete();
//            this.metadata.temp_file.delete(); ??
            this.isWriterFinished = true;
            printAndUpdatePercentageCompleted();
            System.err.println("Download succeeded"); // CHANGE TO ERR
//            metadata.printMap();
//
//            System.out.print("from writer: [");
//
//            for(int i = 0; i < (metadata.pieceMap.bitmap).length; i++){
//                System.out.print(metadata.pieceMap.bitmap[i] +", ");
//            }
//            System.out.println("]");


        } catch (IOException e){
            System.err.println("Access to file failed " + e.getMessage() + ",Download failed");
            return;
        }

    }

    public int getNumOfReadPieces(){
        int count = 0;
        for (int i = 0; i < this.metadata.pieceMap.bitmap.length; i++){
            if(this.metadata.pieceMap.bitmap[i]) count++;
        }
        return count;
    }

    public int getPercentageCompleted(){
        int percentage = (getNumOfReadPieces()*100) / this.metadata.pieceMap.bitmap.length;
        return percentage;
    }

    public boolean isWriterFinished(){
     return isWriterFinished;
    }

    public void printAndUpdatePercentageCompleted(){
        int currentPercentage = getPercentageCompleted();
        if (currentPercentage > this.downloadPrecentage){ // the percentage is rounded down into int
            System.err.println("Downloaded " + currentPercentage  + "%");
        }
        this.downloadPrecentage = currentPercentage;
    }
}
