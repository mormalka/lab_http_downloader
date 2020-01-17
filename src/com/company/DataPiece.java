package com.company;

import java.util.ArrayList;

public class DataPiece {
    public int size;
    public int offset;
//    private int serialNumber;
    public byte[] content;

    public DataPiece(int offset, byte[] data, int pieceSize) {
        this.offset = offset;
        this.content = data;
        this.size = pieceSize;
    }
}
