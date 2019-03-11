public class Chunk {

    private int chunkNo;
    private String fileId;
    private String body;
    private int size;
    private int deseReplicationDegree;
    private int currReplicationDegree = 0;

    public Chunk(String fileId, int chunkNo, int deseReplicationDegree, byte[] buffer) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.deseReplicationDegree = deseReplicationDegree;
        this.body = new String(buffer);
        this.size = buffer.length;
    }


}
