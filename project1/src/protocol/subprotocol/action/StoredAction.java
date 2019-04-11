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
        Chunk chunk = new Chunk(chunkNo);
        String chunkKey = chunk.buildChunkKey(fileId);
        Peer.getDataContainer().incStoredCurrRepDegree(chunkKey);
        //if chunk is not stored chunk
        if(Peer.getDataContainer().getStoredCurrRepDegree(chunkKey) == -1)
          Peer.getDataContainer().incBackedUpChunkCurrRepDegree(chunkKey);
    }
}
