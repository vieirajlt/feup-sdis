package protocol;

class FileInfo {

    private String name;
    private int nrOfChunks;

    public FileInfo(String name, int nrOfChunks) {
        this.name = name;
        this.nrOfChunks = nrOfChunks;
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
}
