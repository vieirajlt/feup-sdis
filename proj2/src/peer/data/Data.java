package peer.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Data implements Serializable {

    private final static String DATA_PATH = "TMP/peer/data/data.ser";

    private final static long INITIAL_STORAGE_CAPACITY = 1000000;

    private int id;

    // maximum amount of disk space that can be used to store chunks (in Bytes)
    private long storageCapacity;

    // amount of disk space being used to store chunks (in Bytes)
    private long currStorageAmount;

    // Key = peer ap
    // Value = available size
    private ConcurrentHashMap<String, Integer> peers;

    private Data(int id) {
        peers = new ConcurrentHashMap<>();
        this.id = id;
        storageCapacity = INITIAL_STORAGE_CAPACITY;
        currStorageAmount = 0;
    }

    public void store() {
        Path path = Paths.get(DATA_PATH);
        try {
            Files.createDirectories(path.getParent());
            FileOutputStream fOut = new FileOutputStream(getDataPath(id), false);
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

    public static Data load(int id) {
        try (FileInputStream fIn = new FileInputStream(getDataPath(id)); ObjectInputStream oIn = new ObjectInputStream(fIn)) {
            return (Data) oIn.readObject();
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Data(id);
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

    public float getCompletionPercentage() {
        return currStorageAmount / storageCapacity;
    }

    public void storeInitPeer(String key, Integer value) {
        peers.put(key, value);
    }

    public static String getDataPath(int id) {
        return "TMP/peer" + id + "/data/data.ser";
    }
}
