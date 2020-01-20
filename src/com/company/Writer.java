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
    public Metadata metadata;
    public boolean isWriterFinished = false;

    public Writer (BlockingQueue<DataPiece> queue, int file_len, File file, Metadata metadata){
        this.queue = queue;
        this.file_len = file_len;
        this.dest_file = file;
        this.metadata = metadata;
    }

    @Override
    public void run() {
        System.out.println("Writer's running..."); // REMOVE
        try{
            RandomAccessFile randomAccess = new RandomAccessFile(dest_file, "rw");
            while(numOfReadBytes < file_len){
                if(!this.queue.isEmpty()){
                    DataPiece dataPiece = queue.poll();
                    randomAccess.seek(dataPiece.offset);
                    randomAccess.write(dataPiece.content);
                    this.metadata.approvePiece(dataPiece.id);
                    numOfReadBytes += dataPiece.size;
                }

            }
            this.isWriterFinished = true;
            metadata.printMap();
//            System.out.print("[");
//            for(int i = 0; i < (metadata.pieceMap.bitmap).length; i++){
//                System.out.print(metadata.pieceMap.bitmap[i] +", ");
//            }
//            System.out.println("]");

            System.out.print("from writer: [");
            for(int i = 0; i < (metadata.pieceMap.bitmap).length; i++){
                System.out.print(metadata.pieceMap.bitmap[i] +", ");
            }
            System.out.println("]");


        } catch (IOException e){
            System.err.println("Access to file failed " + e.getMessage() + ",Download failed");
            return;
        }

    }

    public boolean isWriterFinished(){
     return isWriterFinished;
    }
}
