# lab_http_downloader

Authors

Adi Benoz, ID-302340187
Mor Malka, ID-311142905



Classes

IdcDm:
Runs the main of the java program
Creates an instance of Manager which manage the download process.

Manager:
Initialize workers, single writer threads and metadta, exit the program (code 1) in case of a failer.

Worker:
Create http connection, download range in DataPiece(constant size) and write it to blocking queue.

Writer
Reads DataPiece from blocking queue, maintain metaData instance and write content into new file.

DataPiece
A Serializable object, indicates the safe transfferd DataPieces which written by the writer.

Metadata
Indicated the download state, in case of first download or resume download. 

