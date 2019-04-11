package protocol;

import protocol.subprotocol.fileManagement.FileManager;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.*;

public class Chunk implements Serializable {

    static final long serialVersionUID = 42L;

    private int chunkNo;
    private byte[] body;
    private int size;

    private boolean loaded;

    private static String pathname = "TMP/peer" + Peer.getServerId() + "/backup/";

    public Chunk() {
    }

    public Chunk(int chunkNo, byte[] body) {
        this.chunkNo = chunkNo;
        this.body = body;
        this.size = body.length;
        loaded = true;
    }

    public Chunk(int chunkNo) {
        this.chunkNo = chunkNo;
        this.body = null;
        this.size = 0;
        loaded = true;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public byte[] getBody() {
        return body;
    }

    public void store(String fileId) {
        String chunkId = buildChunkId();
        Path path = Paths.get(pathname + fileId + "/" + chunkId);

        try {
            Files.createDirectories(path.getParent());

            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            ByteBuffer buffer = ByteBuffer.wrap(body);

            fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer,ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    System.out.println("Success writing chunk body!");
                    loaded = true;
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("Error writing chunk body...");
                    loaded = false;
                }
            });
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(String fileId) {
        loaded = false;
        String chunkId = buildChunkId();
        Path path = Paths.get(pathname + fileId + "/" + chunkId);

        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(FileManager.MAX_CHUNK_SIZE * 2);

            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    System.out.println("Success reading chunk body!");
                    attachment.flip();
                    body = new byte[attachment.limit()];
                    attachment.get(body);
                    size = body.length;
                    attachment.clear();
                    loaded = true;
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("Error reading chunk body...");
                }
            });
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String buildChunkKey(String fileId) {
        return fileId + "_" + chunkNo;
    }

    public String buildChunkId() {
        return "chk" + chunkNo;
    }

    public void delete(String fileId) {
        String chunkId = buildChunkId();
        Path path = Paths.get(pathname + fileId + "/" + chunkId);
        try {
            Files.deleteIfExists(path);
            Files.deleteIfExists(path.getParent());
        } catch (DirectoryNotEmptyException e) {
            //do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPathname() {
        return pathname;
    }

    public static String getChunkFolderPath(String fileId) {
        return pathname + fileId + "/";
    }

    public boolean isLoaded() {
        return loaded;
    }
}
