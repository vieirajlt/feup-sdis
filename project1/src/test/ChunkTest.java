package test;

import protocol.Chunk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChunkTest {

    @Test
    public void saveChunckTest() {

        int chunkNo = 34;
        byte[] arr = ("this is a test wdhbfer 98324y23 €€€][]").getBytes();
        Chunk chunk = new Chunk(chunkNo, arr);

        String testname = "testname";
        chunk.store(testname);

        chunk.load(testname, chunkNo);

        assertEquals(chunkNo,chunk.getChunkNo());
        assertEquals(arr,chunk.getBody());
        assertEquals(0,chunk.getCurrReplicationDegree());

        chunk.delete(testname);
    }

}