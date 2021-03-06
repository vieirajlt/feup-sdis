package protocol.subprotocol.fileManagement;

import protocol.Chunk;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public final static int MAX_CHUNK_SIZE = 64000;
    public final static int MAX_NUM_CHUNKS = 1000000;

    private String fileId;
    private List<Chunk> chunks = new ArrayList<>();
    private int chunksSize = 0;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
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

    public static void clearEmptyFolders(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        if(!path.equals(dir))
                            Files.delete(dir);
                    } catch (DirectoryNotEmptyException e) {
                        clearEmptyFolders(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    //do nothing
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
