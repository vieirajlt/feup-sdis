package protocol;

import java.io.*;

public class Chunk implements Serializable {

    static final long serialVersionUID = 42L;

    private int chunkNo;
    private byte[] body;
    private int size;

    private static String pathname = "TMP/" + Peer.getServerId() + "/backup/";

    public Chunk() {}

    public Chunk(int chunkNo, byte[] body) {
        this.chunkNo = chunkNo;
        this.body = body;
        this.size = body.length;
    }

    public Chunk(int chunkNo) {
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
        String chunkId = buildChunkFileId(chunkNo);
        try {
            File file = new File(pathname + fileId + "/" + chunkId);
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
        String chunkId = buildChunkFileId(chunkNo);
        try (
                FileInputStream fIn = new FileInputStream(pathname + fileId + "/" + chunkId);
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

    public static String buildChunkKey(String fileId, int chunkNo) {
        return fileId + "_" + chunkNo;
    }

    public static String buildChunkId(int chunkNo) {
        return "chk" + chunkNo;
    }

    public static String buildChunkFileId(int chunkNo) {
        return buildChunkId(chunkNo) + ".ser";
    }

    public void delete(String fileId) {
        String chunkId = buildChunkFileId(chunkNo);
        File file = new File(pathname + fileId + "/" + chunkId);
        file.delete();
    }

    public static String getPathname() {
        return pathname;
    }

    public static String getChunkFolderPath(String fileId) {
        return pathname + fileId + "/";
    }
}
