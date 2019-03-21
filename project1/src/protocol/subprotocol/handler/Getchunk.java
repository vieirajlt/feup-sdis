package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;
import protocol.SplitFile;

import static protocol.subprotocol.Subprotocol.GETCHUNK;

public class Getchunk extends Handler implements Runnable{

    private static final int MAX_PUTCHUNK_REPEAT = 5;
    private static final int PUTCHUNK_INBETWEEN_TIME_MS = 1000;

    private int repeatCnt = 0;
    private boolean repDone = false;
    private SplitFile sf;

    public Getchunk(SplitFile sf) {
        this.sf = sf;
    }

    @Override
    public void run() {
        System.out.println("protocol.subprotocol.handler.getchunk.run");
        int chunksSize = Peer.getDataContainer().getNrOfChunks(sf.getFileId());
        for(int chunkNo = 0; chunkNo < chunksSize; chunkNo++ )
        {
            String message = buildMessage(GETCHUNK, MSG_CONFIG_GETCHUNK, sf.getFileId(), chunkNo, -1,null);
            Peer.getControlChannel().write(message);
        }
    }
}
