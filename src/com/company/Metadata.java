package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Metadata {

    public PieceMap pieceMap;
    public String metadata_filename = "mapBackup";
    public String temp_filename = "temp_mapBackup";
    public File temp_file;
    public File metadata_file;
//    public String filename;

    public Metadata (int numberOfPieces){
//        this.metadata_file = new File("./" + "mapBackup.txt");
        this.metadata_file = new File("C:\\Test\\" + "mapBackup");

        if(this.metadata_file.exists()){
           readPieceMapFromDisk();
        } else {
            this.pieceMap = new PieceMap(numberOfPieces);
            try {
                this.metadata_file.createNewFile();
            } catch (IOException e){
                System.err.println("Creating new metadata file failed " + e.getMessage() + ",Download failed");
            }
        }

        this.temp_file = new File("C:\\Test\\" + "temp_mapBackup");
        if(!(this.temp_file.exists())){
            try {
                this.temp_file.createNewFile();
            } catch (IOException e){
                System.err.println("Creating new temp file failed " + e.getMessage() + ",Download failed");
            }
        }
    }
    public void approvePiece(int pieceId){
        pieceMap.bitmap[pieceId] = true;
        writePieceMapToDisk();

    }

    public void writePieceMapToDisk(){
        // Serialization for temp file
        try {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(this.temp_file);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(this.pieceMap);

            out.close();
            file.close();

            //atomic write to metadata
            Path temp_path = temp_file.toPath();
            Path metadata_path = metadata_file.toPath();

            Files.move(temp_path,metadata_path, StandardCopyOption.ATOMIC_MOVE);


        } catch(IOException e) {
            System.err.println("IOException is caught " + e);
        }
    }

    public void readPieceMapFromDisk(){

        // Deserialization
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream("C:\\Test\\" + "mapBackup");
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            this.pieceMap = (PieceMap) in.readObject();

            in.close();
            file.close();

            System.out.println("Object has been deserialized ");

        }

        catch(IOException | ClassNotFoundException ex)
        {
            System.err.println("IOException is caught " + ex);
        }

    }

    public void printMap(){ // for testing metadata
        readPieceMapFromDisk();
        System.out.print("from metadata after reading from disk :[");
        for(int i = 0; i < (pieceMap.bitmap).length; i++){
            System.out.print(pieceMap.bitmap[i] +", ");
        }
        System.out.println("]");
    }


}
