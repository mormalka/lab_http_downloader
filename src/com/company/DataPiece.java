package com.company;

import java.util.ArrayList;

public class DataPiece {
    private int size;
    private int offset;
//    private int serialNumber;
    private ArrayList<Byte> content;

    public void DataPiece(int offset, ArrayList<Byte> data, int rangeToRead) {
        this.offset = offset;
        this.content = data;
        this.size = rangeToRead;
    }
}
