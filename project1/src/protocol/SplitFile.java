package protocol;

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

public class SplitFile {

    public final static int MAX_CHUNK_SIZE = 64000;

    private ArrayList<Chunk> chunks;
    private String pathname;
    private String fileId;
    private int replicationDegree;
    private File file;

    /**
     *
     * @param pathname
     * @param replicationDegree
     */
    public SplitFile(String pathname, int replicationDegree) {
        this.chunks = new ArrayList<>();
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
        file = new File(pathname);

        buildId();
        split();
    }

    public static void main(String[] args) {

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        SplitFile sf = new SplitFile("FILES/img.png", 1);

    }

    private void split() {

        int chunkNo = 0;

        byte[] buffer = new byte[MAX_CHUNK_SIZE];

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int prevBytesRead = 0, bytesRead = 0;
            while (bytesRead > 0) {

                byte[] body = Arrays.copyOf(buffer, bytesRead);
                Chunk chunk = new Chunk(chunkNo++, body);

                chunks.add(chunk);

                prevBytesRead = bytesRead;
                Arrays.fill(buffer, (byte)0);
                bytesRead = bis.read(buffer);
            }

            //case last chunk build is full sized
            //add other empty chunk
            if(prevBytesRead == MAX_CHUNK_SIZE) {
                Chunk chunk = new Chunk(chunkNo, new byte[0]);
                chunks.add(chunk);
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

            fileId = sha256(fileIdUnhashed);

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

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }
}
