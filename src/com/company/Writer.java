package com.company;

import java.util.concurrent.BlockingQueue;

public class Writer implements Runnable {

    public BlockingQueue<DataPiece>queue;

    public Writer (BlockingQueue<DataPiece> queue){
        this.queue = queue;
    }

    @Override
    public void run() {

    }
}
