package test;

import org.junit.Test;
import protocol.Chunk;
import protocol.subprotocol.FileManagement.ChunkInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

class ChunkInfoTest {

    @Test
    public void compareToTest() {

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo", 2,2);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo", 2,2);
        ChunkInfo chunkInfo3 = new ChunkInfo("chunkInfo", 2,3);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), 0);
        assertEquals(chunkInfo1.compareTo(chunkInfo3), 1);

    }
}