package protocol;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class DataContainer implements Serializable {

    private final static String DATA_PATH = "TMP/Data/" + Peer.getServerId() + "/" + "data.ser";

    private final static int INITIAL_STORAGE_CAPACITY = 1000000;


    // Key = chunkId
    // Value = currReplicationDegree
    private ConcurrentHashMap<String, Integer> stored;

    // Key = chunkId
    // Value = 0 - desiredRepDegree 1 - currRepDegree
    private ConcurrentHashMap<String, ArrayList<Integer>> backedUpChunks;


    // Key = fileId
    // Value = nrOfChunks
    private ConcurrentHashMap<String, Integer> ownFiles;

    // Key = chunkId
    // Value = shipping state
    private ConcurrentHashMap<String, Boolean> peersChunks;

    // Key = fileId
    // Value = chunks
    private ConcurrentHashMap<String, ArrayList<Chunk>> tmpChunks;

    // maximum amount of disk space that can be used to store chunks (in KBytes)
    private double storageCapacity;

    // amount of storage used to backup the chunks (in KBytes)
    private double currStorageAmount;

    private DataContainer() {
        stored = new ConcurrentHashMap<>();
        backedUpChunks = new ConcurrentHashMap<>();
        ownFiles = new ConcurrentHashMap<>();
        peersChunks = new ConcurrentHashMap<>();
        tmpChunks = new ConcurrentHashMap<>();
        storageCapacity = INITIAL_STORAGE_CAPACITY;
        currStorageAmount = 0;
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
        /*if (stored.get(key) == null)
            stored.put(key, 1);
        else
            stored.replace(key, stored.get(key) + 1);*/
        if(stored.get(key)!= null)
            stored.replace(key, stored.get(key) + 1);
    }

    public void deleteStoredChunk(String key) { stored.remove(key); }

    public void addBackedUpChunk(String key, int desiredRepDegree) {
        if(backedUpChunks.get(key) == null)
        {
            ArrayList<Integer> degrees = new ArrayList<>();
            degrees.add(0,desiredRepDegree);
            degrees.add(1,1);
            backedUpChunks.put(key, degrees);
        }
        System.out.println("addBackedUpChunk");

        backedUpChunks.forEach((k, v) -> {
            System.out.println("" + k + "" + v);
        });
    }

    public void incBackedUpChunkCurrRepDegree(String key) {
        if(backedUpChunks.get(key)== null)
            return;
        backedUpChunks.get(key).set(1, backedUpChunks.get(key).get(1) + 1);
        System.out.println("incBackedUpChunkCurrRepDegree");
        backedUpChunks.forEach((k, v) -> {
            System.out.println("" + k + "" + v);        });
    }

    public void deleteBackedUpChunk(String key) { backedUpChunks.remove(key); }


    public int getDifferenceBtCurrDesiredRepDegrees(String key) {
       return backedUpChunks.get(key).get(1) - backedUpChunks.get(key).get(0);
    }

    public Integer getNrOfChunks(String key) {
        return ownFiles.get(key);
    }

    public Boolean getChunkShippingState(String key) {
        return peersChunks.get(key);
    }

    public void deletePeersFileChunks(String fileId)
    {
        String chunkFileId;
        for(String key : peersChunks.keySet()) {
            chunkFileId = key.split("-")[0];
            if(chunkFileId.equals(fileId))
                peersChunks.remove(key);
        }
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

    public double getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(int storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    public double getCurrStorageAmount() {
        return currStorageAmount;
    }

    public boolean incCurrStorageAmount(double amountOfstorage) {
        double newStorageAmount = this.currStorageAmount + amountOfstorage;
        if(newStorageAmount > this.storageCapacity)
            return false;
        this.currStorageAmount = newStorageAmount;
        return true;
    }

    public void decCurrStorageAmount(double amountOfstorage) {
        this.currStorageAmount -= amountOfstorage;
    }


}
