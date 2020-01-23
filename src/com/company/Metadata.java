package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Metadata {

    public PieceMap pieceMap;
    public File temp_file;
    public File metadata_file;
    public boolean isFirstRun = true;


    public Metadata (int numberOfPieces, String downloadedFileName){
        String currentDir = new File("").getAbsolutePath();
//        this.metadata_file = new File(currentDir + "\\" + downloadedFileName + ".metadata");
        this.metadata_file = new File("C:\\test" + "\\" + downloadedFileName + ".metadata");
        System.out.println("***PATH: "  + this.metadata_file.getAbsolutePath());

        if(this.metadata_file.exists()){
           readPieceMapFromDisk();
            isFirstRun = false;
        } else {
            this.pieceMap = new PieceMap(numberOfPieces);
            try {
                this.metadata_file.createNewFile();
            } catch (IOException e){
                System.err.println("Creating new metadata file failed " + e.getMessage() + ",Download failed");
            }
        }

        this.temp_file = new File("C:\\test" + "\\" + downloadedFileName + ".temp_metadata");
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
            FileOutputStream file = new FileOutputStream(this.temp_file.getAbsolutePath());
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(this.pieceMap);

            out.close();
            file.close();

            //atomic write to metadata
            Path temp_path = temp_file.toPath();
            Path metadata_path = metadata_file.toPath();

            try{
                Files.move(temp_path,metadata_path, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                System.err.println("move function error " +e.getMessage());
            }


        } catch(IOException e) {
            System.err.println("IOException is caught " + e);
        }
    }

    public void readPieceMapFromDisk(){

        // Deserialization
        try
        {
            // Reading the object from a file
//            FileInputStream file = new FileInputStream(this.metadata_file);
            FileInputStream file = new FileInputStream(this.metadata_file);
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
