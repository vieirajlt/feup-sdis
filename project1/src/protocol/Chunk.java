package protocol;

import java.io.*;

public class Chunk implements Serializable {

    private final static String STORE_PATH = "STORED/";

    private int chunkNo;
    private byte[] body;
    private int size;


    private int currReplicationDegree = 0;

    public Chunk(int chunkNo, byte[] body) {
        this.chunkNo = chunkNo;
        this.body = body;
        this.size = body.length;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public byte[] getBody() {
        return body;
    }

    public int getSize() {
        return size;
    }

    public int getCurrReplicationDegree() {
        return currReplicationDegree;
    }

    public void store(String fileId) {
        String chunkId = fileId + "_" + chunkNo + ".ser";
        try {
            File file = new File(STORE_PATH + chunkId);
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file, false);
            ObjectOutputStream oOut = new ObjectOutputStream(fOut);
            oOut.writeObject(this);
            oOut.close();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Chunk load(String fileId, int chunkNo) {
        Chunk loaded = null;
        String chunkId = fileId + "_" + chunkNo + ".ser";

        try (
                FileInputStream fIn = new FileInputStream(STORE_PATH + chunkId);
                ObjectInputStream oIn = new ObjectInputStream(fIn)) {
            loaded = (Chunk) oIn.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return loaded;
    }

    public void delete(String fileId) {
        String chunkId = fileId + "_" + chunkNo + ".ser";
        File file = new File(STORE_PATH + chunkId);
        file.delete();
    }

}
