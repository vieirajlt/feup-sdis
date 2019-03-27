package protocol;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class DataContainer implements Serializable {

    private final static String DATA_PATH = "TMP/Data/" + Peer.getServerId() + "/" + "data.ser";

    // Key = chunkId
    // Value = currReplicationDegree
    private ConcurrentHashMap<String, Integer> stored;

    // Key = fileId
    // Value = nrOfChunks
    private ConcurrentHashMap<String, Integer> ownFiles;

    // Key = chunkId
    // Value = shipping state
    private ConcurrentHashMap<String, Boolean> peersChunks;
    
    // Key = fileId
    // Value = chunks
    private ConcurrentHashMap<String, ArrayList<Chunk>> tmpChunks;

    private DataContainer() {
        stored = new ConcurrentHashMap<>();
        ownFiles = new ConcurrentHashMap<>();
        peersChunks = new ConcurrentHashMap<>();
        tmpChunks = new ConcurrentHashMap<>();
    }

    public void store() {
        tmpChunks.clear();
        File file = new File(DATA_PATH);
        file.getParentFile().mkdirs();
        try {
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

    public static DataContainer load() {
        try (FileInputStream fIn = new FileInputStream(DATA_PATH); ObjectInputStream oIn = new ObjectInputStream(fIn)) {
            return (DataContainer) oIn.readObject();
        } catch (FileNotFoundException e) {
            // Do nothing
            System.out.print("error loading data container");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new DataContainer();
    }

    public Integer getCurrRepDegree(String key) {
        if (stored.get(key) == null)
            stored.put(key, 0);
        return stored.get(key);
    }

    public void incCurrReoDegree(String key) {
        if (stored.get(key) == null)
            stored.put(key, 1);
        else
            stored.replace(key, stored.get(key) + 1);
    }

    public void deleteStoredChunk(String key) { stored.remove(key); }

    public Integer getNrOfChunks(String key) {
        return ownFiles.get(key);
    }

    public Boolean getChunkShippingState(String key) {
        return peersChunks.get(key);
    }

    public void addOwnFile(String key, int nrOfChunks)
    {
        if(ownFiles.get(key) == null)
            ownFiles.put(key, nrOfChunks);
    }

    public void deleteOwnFile(String key) { ownFiles.remove(key); }

    public void addPeerChunk(String key)
    { 
        if(peersChunks.get(key) == null)
          peersChunks.put(key, false);
    }

    public void setPeerChunk(String key, boolean state )
    {
        peersChunks.put(key, false);
        //peersChunks.replace(key, state);
    }


    public  ArrayList<Chunk> getTmpChunksChunks(String fileId) {
        return tmpChunks.get(fileId);
    }

    public void iniTmpChunksChunks(String fileId, int chunksSize) {
         tmpChunks.put(fileId, new ArrayList<>(Collections.nCopies(chunksSize, null)));
    }

    public boolean isTmpChunksChunksComplete(String fileId) {
        ArrayList<Chunk> chunks = tmpChunks.get(fileId);
        for(Chunk chunk : chunks) {
            if(chunk == null)
                return false;
        }
        return true;
    }


}
