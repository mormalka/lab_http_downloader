package com.company;

import java.util.ArrayList;

public class DataPiece {
    private int size;
    private int offset;
//    private int serialNumber;
    private ArrayList<Integer> content;

    public DataPiece(int offset, ArrayList<Integer> data, int rangeToRead) {
        this.offset = offset;
        this.content = data;
        this.size = rangeToRead;
    }
}
