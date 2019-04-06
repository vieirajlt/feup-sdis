package protocol.info;

import java.io.Serializable;

public class FileInfo implements Serializable {

    private String name;
    private int nrOfChunks;
    private int repDegree;

    public FileInfo(String name, int nrOfChunks, int repDegree) {
        this.name = name;
        this.nrOfChunks = nrOfChunks;
        this.repDegree = repDegree;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNrOfChunks() {
        return nrOfChunks;
    }

    public void setNrOfChunks(int nrOfChunks) {
        this.nrOfChunks = nrOfChunks;
    }

    public int getRepDegree() {
        return repDegree;
    }

    public void setRepDegree(int repDegree) {
        this.repDegree = repDegree;
    }


}
