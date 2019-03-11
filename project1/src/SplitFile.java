import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class SplitFile {

    public final static int MAX_CHUNK_SIZE = 64000;

    private ArrayList<Chunk> chunks;
    private String pathname;
    private int replicationDegree;

    public static void main(String[] args) {
        try {
            String test = sha256("TEST");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public SplitFile(String pathname, int replicationDegree) {
        this.chunks = new ArrayList<>();
        this.pathname = pathname;
        this.replicationDegree = replicationDegree;
    }

    private void split() {
        File f = new File(pathname);

        int chunkNo = 0;

        byte[] buffer = new byte[MAX_CHUNK_SIZE];

        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while (bytesAmount > 0) {

                //Chunk chunk = new Chunk();
                bytesAmount = bis.read(buffer);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildId() {

    }

    static String sha256(String input) throws NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = messageDigest.digest(input.getBytes());

        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < result.length; i++) {
            //build hex String, forcing 3 digits
            String hexVal = Integer.toHexString((result[i] & 0xff) | 0x100);
            //append only least significant 2 digits
            stringBuffer.append(hexVal.substring(1));
        }

        return stringBuffer.toString();
    }
}
