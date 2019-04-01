package test;

import org.junit.Test;
import protocol.subprotocol.FileManagement.ChunkInfo;

import static org.junit.Assert.*;

public class ChunkInfoTest {

    @Test
    public void compareToTest1() {

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 2,2);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 2,2);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), 0);
    }

    @Test
    public void compareToTest2() {
        /*if(this_difference > 0 && o_difference <= 0)
            return 1;*/

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 2,3);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 2,2);
        ChunkInfo chunkInfo3 = new ChunkInfo("chunkInfo_3", 2,1);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), 1);
        assertEquals(chunkInfo1.compareTo(chunkInfo3), 1);
    }


    @Test
    public void compareToTest3() {
        /*if(this_difference > 0 && o_difference > 0 )
            return this_difference.compareTo(o_difference);*/

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 2,4);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 2,3);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), 1);
    }

    @Test
    public void compareToTest4() {
        /* if(this_difference == 0 && o_difference < 0)
            return 1;*/

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 2,2);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 3,2);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), 1);
    }

    @Test
    public void compareToTest5() {
        /*  if(this_difference == 0 && o_difference > 0)
            return -1*/

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 2,2);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 2,3);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), -1);
    }

    @Test
    public void compareToTest6() {
        /*
        if(this_difference == 0 && o_difference == 0)
            return currRepDegree.compareTo(o.getCurrRepDegree());*/

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 4,4);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 2,2);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), 1);
    }

    @Test
    public void compareToTest7() {
        /*
        if(this_difference < 0 && o_difference < 0)
            return currRepDegree.compareTo(o.getCurrRepDegree());*/

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 5,4);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 3,2);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), 1);
    }

    @Test
    public void compareToTest8() {
        /*
        if(this_difference < 0 && o_difference >= 0)
            return -1;*/

        ChunkInfo chunkInfo1 = new ChunkInfo("chunkInfo_1", 5,4);
        ChunkInfo chunkInfo2 = new ChunkInfo("chunkInfo_2", 4,4);
        ChunkInfo chunkInfo3 = new ChunkInfo("chunkInfo_3", 4,5);

        assertEquals(chunkInfo1.compareTo(chunkInfo2), -1);
        assertEquals(chunkInfo1.compareTo(chunkInfo2), -1);
    }

}