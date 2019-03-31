package protocol.subprotocol.FileManagement;

import protocol.Chunk;

import java.util.ArrayList;

public class FileManager {

    /*Must be maxed to 64000 to be accordingly to max
    chunk size. Not possible with UDP. TODO on RMI
     */
    public final static int MAX_CHUNK_SIZE = 1000;

    private  String fileId;
    private ArrayList<Chunk> chunks = new ArrayList<>();
    private int chunksSize = 0;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
        this.chunksSize = chunks.size();
    }

    public void addChunksChunk(Chunk chunk) {
        this.chunks.add(chunk);
        ++this.chunksSize;
    }

    public int getChunksSize() {
        return chunksSize;
    }

    public void setChunksSize(int chunksSize) {
        this.chunksSize = chunksSize;
    }
}
