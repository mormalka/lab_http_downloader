package com.company;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

public class Writer implements Runnable {

    public BlockingQueue<DataPiece>queue;
    public int file_len;
    public int numOfReadBytes = 0; //num of bytes that the writer already read
    public File dest_file;

    public Writer (BlockingQueue<DataPiece> queue, int file_len, File file){
        this.queue = queue;
        this.file_len = file_len;
        this.dest_file = file;
    }

    @Override
    public void run() {
        System.out.println("Writer's running...");
        try{
            RandomAccessFile randomAccess = new RandomAccessFile(dest_file, "rw");
            while(numOfReadBytes < file_len){
                if(!this.queue.isEmpty()){
                    DataPiece dataPiece = queue.poll();
                    randomAccess.seek(dataPiece.offset);
                    randomAccess.write(dataPiece.content);
                    numOfReadBytes += dataPiece.size;
                    System.out.println("***numOfReadBytes: " + numOfReadBytes);
                }

            }
        } catch (IOException e){
            System.err.println("Access to file failed " + e.getMessage() + ",Download failed");
            return;
        }

    }
}
