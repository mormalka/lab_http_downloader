package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Metadata {

    public PieceMap pieceMap;
//    public String metadata_filename = "mapBackup";
//    public String temp_filename = "temp_mapBackup";
    public File temp_file;
    public File metadata_file;
    public String filename;

    public Metadata (int numberOfPieces, String filename){
        this.filename = filename;
        File file = new File("./" + this.filename);

        if(file.exists()){
           readPieceMapFromDisk();
        }
        //---------------------------------------------------
        //create new mapBackup file and initialize it
        this.pieceMap = new PieceMap(numberOfPieces);
        


    }
    public void approvePiece(int pieceId){
        pieceMap.bitmap[pieceId] = true;
        writePieceMapToDisk();

    }

    public void writePieceMapToDisk(){
        // Serialization for temp file
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream("temp_mapBackup");
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(this.pieceMap);

            out.close();
            file.close();

            ///--------------------------------------------------
            //atomic write to metadata
            Path temp_path = temp_file.toPath();
            Path metadata_path = metadata_file.toPath();

            Files.move(temp_path,metadata_path, StandardCopyOption.ATOMIC_MOVE);

            System.out.println("Object has been serialized");

        } catch(IOException e) {
            System.err.println("IOException is caught");
        }
    }

    public void readPieceMapFromDisk(){

        // Deserialization
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream("mapBackup");
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            pieceMap = (PieceMap) in.readObject();

            in.close();
            file.close();

            System.out.println("Object has been deserialized ");

        }

        catch(IOException | ClassNotFoundException ex)
        {
            System.err.println("IOException is caught");
        }

    }


}
