package protocol.info;

import protocol.Chunk;
import protocol.Peer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataContainer implements Serializable {

    private final static String DATA_PATH = "TMP/peer" + Peer.getServerId() + "/data/data.ser";

    private final static long INITIAL_STORAGE_CAPACITY = 1000000;

    // Key = chunkId
    // Value = peers that store chunks
    private ConcurrentHashMap<String, ArrayList<String>> stored; //all the chunks of the Peer's own files

    // Key = fileId
    // Value = nrOfChunks
    private ConcurrentHashMap<String, FileInfo> ownFiles; // all the Peer's own files


    // Key = chunkId
    // Value = 0 - chunkInfo
    private ConcurrentHashMap<String, ChunkInfo> backedUpChunks; //all the chunks the Peer is backing up for another Peer


    // Key = chunkId
    // Value = shipping state
    private ConcurrentHashMap<String, Boolean> peersChunks; //used in restore

    // Key = fileId
    // Value = chunks
    private ConcurrentHashMap<String, ArrayList<Chunk>> tmpChunks; //used in restore

    // Key = fileId_ownerId
    // Value = response (-1 no response, 0 not deleted, 1 deleted)
    private ConcurrentHashMap<String, Integer> tmpBackedUpFiles; //used in delete enhancement

    // maximum amount of disk space that can be used to store chunks (in Bytes)
    private long storageCapacity;
    // amount of disk space being used to store chunks (in Bytes)
    private long currStorageAmount;

    private DataContainer() {
        stored = new ConcurrentHashMap<>();
        backedUpChunks = new ConcurrentHashMap<>();
        ownFiles = new ConcurrentHashMap<>();
        peersChunks = new ConcurrentHashMap<>();
        tmpChunks = new ConcurrentHashMap<>();
        tmpBackedUpFiles = new ConcurrentHashMap<>();
        storageCapacity = INITIAL_STORAGE_CAPACITY;
        currStorageAmount = 0;
    }

    public void store() {
        tmpChunks.clear();
        tmpBackedUpFiles.clear();
        Path path = Paths.get(DATA_PATH);
        try {
            Files.createDirectories(path.getParent());
            FileOutputStream fOut = new FileOutputStream(DATA_PATH, false);
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new DataContainer();
    }


    public void addStored(String key) {
        if (stored.get(key) == null)
            stored.put(key, new ArrayList<>());
    }

    public Integer getStoredCurrRepDegree(String key) {
        if (stored.get(key) == null)
            return -1;
        return stored.get(key).size();
    }

    public ArrayList<String> getStoredPeersList(String key) {
        if (stored.get(key) == null)
            return null;
        return stored.get(key);
    }

    public void incStoredCurrRepDegree(String key, String senderId) {
        /*if (stored.get(key) == null)
            stored.put(key, 1);
        else
            stored.replace(key, stored.get(key) + 1);*/
        if (stored.get(key) != null)
            stored.get(key).add(senderId);
    }

    public void decStoredCurrRepDegree(String key, String senderId) {
        /*if (stored.get(key) == null)
            stored.put(key, 1);
        else
            stored.replace(key, stored.get(key) + 1);*/
        if (stored.get(key) != null)
            stored.get(key).remove(senderId);
    }

    public void deleteStoredChunk(String key) {
        stored.remove(key);
    }

    public void addBackedUpChunk(String key, int senderId, int desiredRepDegree) {
        if (backedUpChunks.get(key) == null) {
            ChunkInfo chunkInfo = new ChunkInfo(key, senderId, desiredRepDegree, 0, false);
            backedUpChunks.put(key, chunkInfo);
        } else
            backedUpChunks.get(key).setDesiredRepDegree(desiredRepDegree);
    }

    public void incBackedUpChunkCurrRepDegree(String key) {
        if (backedUpChunks.get(key) == null)
            return;
        backedUpChunks.get(key).setCurrRepDegree(backedUpChunks.get(key).getCurrRepDegree() + 1);
    }

    public void decBackedUpChunkCurrRepDegree(String key) {
        if (backedUpChunks.get(key) == null)
            return;
        backedUpChunks.get(key).setCurrRepDegree(backedUpChunks.get(key).getCurrRepDegree() - 1);
    }

    public void deleteBackedUpChunk(String key) {
        backedUpChunks.remove(key);
    }

    public void deleteBackedUpFileChunks(String fileId) {
        String chunkFileId;
        for (String key : backedUpChunks.keySet()) {
            chunkFileId = key.split("_")[0];
            if (chunkFileId.equals(fileId))
                backedUpChunks.remove(key);
        }
    }


    public int getDifferenceBtCurrDesiredRepDegrees(String key) {
        return backedUpChunks.get(key).getDifferenceBtCurrDesiredRepDegrees();
    }

    public ConcurrentHashMap<String, ChunkInfo> getBackedUpChunks() {
        return backedUpChunks;
    }

    public int getBackedUpChunkCurrRepDegree(String key) {
        if (backedUpChunks.get(key) == null)
            return -1;
        return backedUpChunks.get(key).getCurrRepDegree();
    }

    public int getBackedUpChunkDesiredRepDegree(String key) {
        if (backedUpChunks.get(key) == null)
            return -1;
        return backedUpChunks.get(key).getDesiredRepDegree();
    }

    public boolean isBackedUpChunkOnPeer(String key) {
        ChunkInfo ci = backedUpChunks.get(key);
        if (ci == null)
            return false;
        return ci.isOnPeer();
    }

    public void setBackedUpChunkOnPeer(String key, boolean onPeer) {
        ChunkInfo ci = backedUpChunks.get(key);
        if (ci == null)
            return;
        ci.setOnPeer(onPeer);
    }

    public int getBackedUpChunkSenderId(String key) {
        ChunkInfo ci = backedUpChunks.get(key);
        if (ci == null)
            return -1;
        return ci.getSenderId();
    }

    public ConcurrentHashMap<String, FileInfo> getOwnFiles() {
        return ownFiles;
    }

    public FileInfo getOwnFile(String key) {
        return ownFiles.get(key);
    }

    public Integer getOwnFileNrOfChunks(String key) {
        return ownFiles.get(key).getNrOfChunks();
    }

    public String getOwnFileName(String key) {
        return ownFiles.get(key).getName();
    }

    public Boolean getChunkShippingState(String key) {
        return peersChunks.get(key);
    }

    public void deletePeersFileChunks(String fileId) {
        String chunkFileId;
        for (String key : peersChunks.keySet()) {
            chunkFileId = key.split("_")[0];
            if (chunkFileId.equals(fileId))
                peersChunks.remove(key);
        }
    }

    public void addOwnFile(String key, String id, String path, int nrOfChunks, int repDegree) {
        if (ownFiles.get(key) == null) {
            FileInfo fi = new FileInfo(id, path, nrOfChunks, repDegree);
            ownFiles.put(key, fi);
        }
    }

    public void deleteOwnFile(String key) {
        ownFiles.remove(key);
    }

    public void addPeerChunk(String key) {
        if (peersChunks.get(key) == null)
            peersChunks.put(key, false);
    }

    public void setPeerChunk(String key, boolean state) {
        //TODO porque que isto estava comentado ao contrario??? Responde meeeeeeeeeeeeeeeeeeeeee
        //peersChunks.put(key, false);
        peersChunks.replace(key, state);
    }


    public ArrayList<Chunk> getTmpChunksChunks(String fileId) {
        return tmpChunks.get(fileId);
    }

    public void iniTmpChunksChunks(String fileId, int chunksSize) {
        tmpChunks.put(fileId, new ArrayList<>(Collections.nCopies(chunksSize, null)));
    }

    public boolean isTmpChunksChunksComplete(String fileId) {
        ArrayList<Chunk> chunks = tmpChunks.get(fileId);
        for (Chunk chunk : chunks) {
            if (chunk == null)
                return false;
        }
        return true;
    }

    public synchronized long getStorageCapacity() {
        return storageCapacity;
    }

    public synchronized void setStorageCapacity(long storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    public synchronized long getCurrStorageAmount() {
        return currStorageAmount;
    }

    public synchronized boolean incCurrStorageAmountAndCheckSpace(long value) {
        if (currStorageAmount + value > storageCapacity)
            return false;
        incCurrStorageAmount(value);
        return true;
    }

    public synchronized void incCurrStorageAmount(long value) {
        currStorageAmount += value;
    }

    public synchronized void decCurrStorageAmount(long value) {
        currStorageAmount -= value;
    }

    public List<ChunkInfo> getBackedUpChunksOnPeerSortedInfo() {
        List<ChunkInfo> sorted = new ArrayList<>(backedUpChunks.values());
        Collections.sort(sorted);
        Collections.reverse(sorted);
        List<ChunkInfo> values = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            //filter on peer elements
            if (sorted.get(i).isOnPeer())
                values.add(sorted.get(i));
        }
        return values;
    }

    public void deleteOwnFileAndChunks(String fileId) {
        if (getOwnFile(fileId) == null)
            return;

        int nrOfChunks = getOwnFileNrOfChunks(fileId);
        deleteOwnFile(fileId);

        String chunkKey;

        for (int chunkNo = 0; chunkNo < nrOfChunks; chunkNo++) {
            Chunk chunk = new Chunk(chunkNo);
            chunkKey = chunk.buildChunkKey(fileId);
            deleteStoredChunk(chunkKey);
        }

    }

    public void setBackedUpChunksChunkInfoHandling(String chunkKey, boolean handling) {
        backedUpChunks.get(chunkKey).setHandling(handling);
    }

    public boolean isBackedUpChunkInfoHandling(String chunkKey) {
        return backedUpChunks.get(chunkKey).isHandling();
    }

    public float getCompletionPercentage() {
        return currStorageAmount / storageCapacity;
    }


    public synchronized int getBackedUpChunkFileOwnerId(String fileId) {
        for (Map.Entry<String, ChunkInfo> entry : backedUpChunks.entrySet()) {
            if (entry.getValue().getFileId().equals(fileId)) {
                return entry.getValue().getSenderId();
            }
        }
        return -1;
    }


    public void addTmpBackedUpFile(String fileId, int ownerId) {
        String key = buildTmpBackedUpFileKey(fileId, ownerId);
        if (tmpBackedUpFiles.get(key) == null)
            tmpBackedUpFiles.put(key, -1);
    }

    public void setTmpBackedUpFileResponse(String fileId, int ownerId, Integer response)  {
        String key = buildTmpBackedUpFileKey(fileId, ownerId);
         if(tmpBackedUpFiles.get(key)!= null)
             tmpBackedUpFiles.replace(key,response);
    }


    public boolean hasTmpBackedUpFile(String fileId, int ownerId)  {
        if(tmpBackedUpFiles.get(buildTmpBackedUpFileKey(fileId, ownerId)) == null)
            return false;
        return true;
    }

    public int getTmpBackedUpFileResponse(String fileId, int ownerId)  {
           return tmpBackedUpFiles.get(buildTmpBackedUpFileKey(fileId, ownerId));
    }

    private String buildTmpBackedUpFileKey(String fileId, int ownerId) {
        return fileId + "_" + ownerId;
    }


}
