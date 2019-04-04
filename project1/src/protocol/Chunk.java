package protocol;

import java.io.*;

public class Chunk implements Serializable {

    public final static String STORE_PATH = "TMP/STORED/";

    static final long serialVersionUID = 42L;

    private int chunkNo;
    private byte[] body;
    private int size;

    private String pathname;

    public Chunk() {
        this.pathname = STORE_PATH + Peer.getServerId() + "/";
    }

    public Chunk(int chunkNo, byte[] body) {
        this.pathname = STORE_PATH + Peer.getServerId() + "/";
        this.chunkNo = chunkNo;
        this.body = body;
        this.size = body.length;
    }

    public Chunk(int chunkNo) {
        this.pathname = STORE_PATH + Peer.getServerId() + "/";
        this.chunkNo = chunkNo;
        this.body = null;
        this.size = 0;
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

    public void store(String fileId) {
        String chunkId = buildChunkFileId(fileId, chunkNo);
        try {
            File file = new File(pathname + chunkId);
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
        String chunkId = buildChunkFileId(fileId, chunkNo);
        try (
                FileInputStream fIn = new FileInputStream(pathname + chunkId);
                ObjectInputStream oIn = new ObjectInputStream(fIn)) {
            loaded = (Chunk) oIn.readObject();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return loaded;
    }

    public static String buildChunkId(String fileId, int chunkNo) {
        return fileId + "_" + chunkNo;
    }

    public String buildChunkFileId(String fileId, int chunkNo) {
        return buildChunkId(fileId, chunkNo) + ".ser";
    }

    public void delete(String fileId) {
        String chunkId = buildChunkFileId(fileId, chunkNo);
        File file = new File(pathname + chunkId);
        file.delete();
    }

    public String getPathname() {
        return pathname;
    }
}
