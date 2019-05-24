package central.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Data implements Serializable {

    private final static String DATA_PATH = "TMP/central/data/data.ser";

    // Key = peer ap
    // Value = available size
    private ConcurrentHashMap<String, Integer> peers;
    // Key = filename
    // Value = peer ap
    private ConcurrentHashMap<String, String> owners;

    private Data() {
        peers = new ConcurrentHashMap<>();
        owners = new ConcurrentHashMap<>();
    }

    public void store() {
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

    public static Data load() {
        try (FileInputStream fIn = new FileInputStream(DATA_PATH); ObjectInputStream oIn = new ObjectInputStream(fIn)) {
            return (Data) oIn.readObject();
        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Data();
    }

    public void storeInitPeer(String key, Integer value) {
        peers.put(key, value);
    }

    public ArrayList<String> findAvailablePeers(String ap, Integer size) {
        ArrayList<String> available = new ArrayList<>();
        for (String key : peers.keySet()) {
            if (!key.equals(ap) && peers.get(key) >= size)
                available.add(key);
        }
        return available;
    }

    public String getOwner(String filename) {
        return owners.get(filename);
    }

    public void addOwner(String key, String value) {
        this.owners.put(key, value);
    }
}
