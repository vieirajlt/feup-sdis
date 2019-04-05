package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;

public class StoredAction extends Action {

    private String fileId;
    private int chunkNo;

    public StoredAction(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void process() {
        String chunkKey = Chunk.buildChunkKey(fileId, chunkNo);
        Peer.getDataContainer().incStoredCurrRepDegree(chunkKey);
        Peer.getDataContainer().incBackedUpChunkCurrRepDegree(chunkKey);
    }
}
