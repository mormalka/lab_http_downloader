package com.company;

import java.io.Serializable;

public class PieceMap implements Serializable {

    public boolean[] bitmap;

    public PieceMap(int numberOfPieces){

        this.bitmap = new boolean[numberOfPieces]; // initial value - False
    }
}
