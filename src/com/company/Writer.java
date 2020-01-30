package com.company;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

public class Writer implements Runnable {

    public BlockingQueue<DataPiece> queue;
    public int file_len;
    public int numOfReadBytes; //num of bytes that the writer already read
    public File dest_file;
    public Metadata metadata;
    public boolean isWriterFinished = false;
    public int downloadPercentage;
    public Manager manager;

    public Writer (BlockingQueue<DataPiece> queue, int file_len, File file, Metadata metadata, Manager manager){
        this.queue = queue;
        this.file_len = file_len;
        this.dest_file = file;
        this.metadata = metadata;
        this.numOfReadBytes = 0;
        this.downloadPercentage = getPercentageCompleted();
        this.manager = manager;
    }


    @Override
    public void run() {
        RandomAccessFile randomAccess = null;
        try {
            if(metadata.isFirstRun){
                System.err.println("Downloaded 0%");
            }
            randomAccess = new RandomAccessFile(dest_file, "rw");
            while (this.downloadPercentage < 100) {
                if (!this.queue.isEmpty()) {
                    DataPiece dataPiece = queue.poll();
                    randomAccess.seek(dataPiece.offset);
                    randomAccess.write(dataPiece.content);
                    this.metadata.approvePiece(dataPiece.id);
                    numOfReadBytes += dataPiece.size;
                    printAndUpdatePercentageCompleted();
                }

            }

            this.metadata.metadata_file.delete();
            this.isWriterFinished = true;
            printAndUpdatePercentageCompleted();
            System.err.println("Download succeeded");

        } catch (IOException e){
            System.err.println("Can not access to file. Download failed");
            manager.handleErrors();
        }finally {
            if(randomAccess != null) {
                try {
                    randomAccess.close();
                } catch (IOException e) {
                    System.err.println("Can not close file.");
                }
            }
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

    public void printAndUpdatePercentageCompleted(){
        int currentPercentage = getPercentageCompleted();
        if (currentPercentage > this.downloadPercentage){ // the percentage is rounded down into int
            System.err.println("Downloaded " + currentPercentage  + "%");
        }
        this.downloadPercentage = currentPercentage;
    }
}
