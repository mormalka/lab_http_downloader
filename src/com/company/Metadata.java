package com.company;

public class Metadata {
    public boolean[] pieceMap;

    public Metadata(int numberOfPieces){
        this.pieceMap = new boolean[numberOfPieces]; // initial value - False
    }

    public void approvePiece(int pieceId){
        pieceMap[pieceId] = true;
    }
}
