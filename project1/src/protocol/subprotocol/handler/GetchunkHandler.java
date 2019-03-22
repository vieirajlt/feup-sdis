package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.subprotocol.FileManagement.SplitFile;

import static protocol.subprotocol.Subprotocol.GETCHUNK;

public class GetchunkHandler extends Handler implements Runnable{

    private SplitFile sf;

    public GetchunkHandler(SplitFile sf) {
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
