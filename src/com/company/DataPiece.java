package com.company;

import java.util.ArrayList;

public class DataPiece {
    private int size;
    private int offset;
//    private int serialNumber;
    private byte[] content;

    public DataPiece(int offset, byte[] data, int rangeToRead) {
        this.offset = offset;
        this.content = data;
        this.size = rangeToRead;
    }
}
