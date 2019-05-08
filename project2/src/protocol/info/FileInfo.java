package protocol.info;

import java.io.Serializable;

public class FileInfo implements Serializable {

    private String name;
    private String path;
    private int nrOfChunks;
    private int repDegree;

    public FileInfo(String name, String path, int nrOfChunks, int repDegree) {
        this.name = name;
        this.path = path;
        this.nrOfChunks = nrOfChunks;
        this.repDegree = repDegree;
    }

    public String getName() {
        return name;
    }

    public int getNrOfChunks() {
        return nrOfChunks;
    }

    public int getRepDegree() {
        return repDegree;
    }

    public String getPath() {
        return path;
    }
}
