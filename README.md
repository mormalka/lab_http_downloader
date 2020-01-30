# lab_http_downloader

Authors

Adi Benoz, ID-302340187
Mor Malka, ID-311142905


Classes

IdcDm:
Runs the main of the java program
Creates an instance of Manager which manage the download process.

Manager:
Initialize workers, single writer threads and metadta, exit the program (code 1) in case of a failure.

Worker:
Create http connection, download range of bytes in DataPieces(constant size) and write it to the blocking queue.

Writer:
Reads DataPieces from blocking queue, write content into new file and maintain metadata (after every piece write).

PieceMap:
A Serializable object, indicates the safe transferred DataPieces which written by the writer.

DataPiece:
Object represents a piece of the download data by id, content, size, and offset - the position in the file of the first byte the content holds. 

Metadata:
Indicates the download state in case of first download or resume download by holding a piece map object, writes it to the disk and read from it. 

