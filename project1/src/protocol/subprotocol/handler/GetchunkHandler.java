package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.subprotocol.fileManagement.SplitFile;

import static protocol.subprotocol.Subprotocol.GETCHUNK;

public class GetchunkHandler extends Handler{

    private SplitFile sf;
    private boolean enhanced;

    public GetchunkHandler(SplitFile sf, boolean enhanced) {
        this.sf = sf;
        this.enhanced = enhanced;
    }

    @Override
    public void handle() {
        System.out.println("protocol.subprotocol.handler.getchunk.run");
        int chunksSize = Peer.getDataContainer().getOwnFileNrOfChunks(sf.getFileId());
        for(int chunkNo = 0; chunkNo < chunksSize; chunkNo++ )
        {
            byte[] message;
            if(enhanced)
                message = buildMessage(GETCHUNK, MSG_CONFIG_GETCHUNK, sf.getFileId(), chunkNo, -1, "ENH");
            else
                message = buildMessage(GETCHUNK, MSG_CONFIG_GETCHUNK, sf.getFileId(), chunkNo, -1, (byte[]) null);
            Peer.getControlChannel().write(message);
        }
    }
}
