package protocol.subprotocol.FileManagement;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.handler.PutchunkHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SplitFile extends FileManager {

    private String pathname;
    private String fileName;
    private int replicationDegree;

    /**
     * @param pathname
     * @param replicationDegree
     */
    public SplitFile(String pathname, int replicationDegree) {
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
        Path path = Paths.get(pathname);
        this.fileName = path.getFileName().toString();
        buildId();

    }

    public SplitFile(String pathname) {
        this.pathname = pathname;
        this.replicationDegree = 1;
        Path path = Paths.get(pathname);
        this.fileName = path.getFileName().toString();
        buildId();
    }

    public void splitAndSend(boolean enhanced) {

        Path path = Paths.get(pathname);

        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(MAX_CHUNK_SIZE * MAX_NUM_CHUNKS);

            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    attachment.flip();
                    Integer position = 0;
                    int chunkNo = 0, length = MAX_CHUNK_SIZE;
                    while (length == MAX_CHUNK_SIZE) {
                        //read chunk body
                        if (result - position < MAX_CHUNK_SIZE) {
                            length = result - position;
                        }

                        byte[] body = new byte[length];
                        if (length != 0) {
                            attachment.get(body);
                            position += length;
                        }

                        //store chunk
                        Chunk chunk = new Chunk(chunkNo++, body);
                        addChunksChunk(chunk);
                        String chunkKey = chunk.buildChunkKey(getFileId());
                        Peer.getDataContainer().addStored(chunkKey);

                        //handler
                        PutchunkHandler putchunkHandler = new PutchunkHandler(chunk, getFileId(), replicationDegree, enhanced);
                        Peer.getExecutor().execute(putchunkHandler);
                    }
                    Peer.getDataContainer().addOwnFile(getFileId(), fileName, getChunksSize(), replicationDegree);
                    attachment.clear();
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.out.println("Error reading file for splitAndSend...");
                }
            });
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildId() {

        try {

            Path path = Paths.get(pathname);

            BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileOwnerAttributeView ownerAttribute = Files.getFileAttributeView(path, FileOwnerAttributeView.class);

            String fileIdUnhashed =
                    this.fileName +
                            ownerAttribute.getOwner() +
                            fileAttributes.size() +
                            fileAttributes.creationTime() +
                            fileAttributes.lastModifiedTime() +
                            Peer.getServerId();

            setFileId(sha256(fileIdUnhashed));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String sha256(String input) {

        try {
            MessageDigest messageDigest = null;
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] result = messageDigest.digest(input.getBytes());

            StringBuffer stringBuffer = new StringBuffer();

            for (int i = 0; i < result.length; i++) {
                //build hex String, forcing 3 digits
                String hexVal = Integer.toHexString((result[i] & 0xff) | 0x100);
                //append only least significant 2 digits
                stringBuffer.append(hexVal.substring(1));
            }

            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPathname() {
        return pathname;
    }
}
