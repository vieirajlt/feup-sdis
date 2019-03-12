public class Chunk {

    private int chunkNo;
    private byte[] body;
    private int size;
    private int currReplicationDegree = 0;

    public Chunk(int chunkNo, byte[] body) {
        this.chunkNo = chunkNo;
        this.body = body;
        this.size = body.length;
    }


}
