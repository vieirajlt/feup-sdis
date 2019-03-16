package protocol;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataContainer implements Serializable {

    private final static String DATA_PATH = "TMP/Data/" + Peer.getServerId() + "/" + "data.ser";

    //Key = chunkId
    //Value = currReplicationDegree
    private ConcurrentHashMap<String, Integer> stored;

    private DataContainer() {
        stored = new ConcurrentHashMap<>();
    }

    public void store() {
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
        try (
                FileInputStream fIn = new FileInputStream(DATA_PATH);
                ObjectInputStream oIn = new ObjectInputStream(fIn)) {
            return (DataContainer) oIn.readObject();
        } catch (FileNotFoundException e) {
            //Do nothing
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
}
