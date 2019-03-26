package protocol.subprotocol.FileManagement;

import protocol.Chunk;
import protocol.subprotocol.FileManagement.FileManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class SplitFile extends FileManager {

    private String pathname;
    private int replicationDegree;
    private File file;

    /**
     * @param pathname
     * @param replicationDegree
     */
    public SplitFile(String pathname, int replicationDegree) {
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
        file = new File(pathname);

        buildId();
        split();
    }

    public SplitFile(String pathname) {
        this.pathname = pathname;
        this.replicationDegree = 1;
        file = new File(pathname);
        buildId();
    }

    private void split() {
        int chunkNo = 0;

        byte[] buffer = new byte[MAX_CHUNK_SIZE];

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int prevBytesRead = 0, bytesRead = bis.read(buffer);
            while (bytesRead > 0) {

                byte[] body = Arrays.copyOf(buffer, bytesRead);
                Chunk chunk = new Chunk(chunkNo++, body);

                addChunksChunk(chunk);

                prevBytesRead = bytesRead;
                Arrays.fill(buffer, (byte) 0);
                bytesRead = bis.read(buffer);
            }

            //case last chunk build is full sized
            //add other empty chunk
            if (prevBytesRead == MAX_CHUNK_SIZE) {
                Chunk chunk = new Chunk(chunkNo, new byte[0]);
                addChunksChunk(chunk);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
                    file.getName() +
                            ownerAttribute.getOwner() +
                            fileAttributes.size() +
                            fileAttributes.creationTime() +
                            fileAttributes.lastModifiedTime();

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

    public File getFile() { return file; }
}
