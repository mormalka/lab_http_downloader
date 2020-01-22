package com.company;

import java.util.ArrayList;

public class DataPiece {
    public int size;
    public int offset;
    public byte[] content;
    public int id;

    public DataPiece(int offset, byte[] data, int pieceSize, int id) {
        this.offset = offset;
        this.content = data;
        this.size = pieceSize;
        this.id = id;
    }
}
