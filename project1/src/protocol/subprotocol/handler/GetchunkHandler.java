package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.subprotocol.fileManagement.SplitFile;

import static protocol.subprotocol.Subprotocol.GETCHUNK;

public class GetchunkHandler extends Handler{

    private SplitFile sf;

    public GetchunkHandler(SplitFile sf) {
        this.sf = sf;
    }

    @Override
    public void handle() {
        System.out.println("protocol.subprotocol.handler.getchunk.run");
        int chunksSize = Peer.getDataContainer().getOwnFileNrOfChunks(sf.getFileId());
        for(int chunkNo = 0; chunkNo < chunksSize; chunkNo++ )
        {
            byte[] message = buildMessage(GETCHUNK, MSG_CONFIG_GETCHUNK, sf.getFileId(), chunkNo, -1, (byte[]) null);
            Peer.getControlChannel().write(message);
        }
    }
}
