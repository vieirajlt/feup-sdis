package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;

public class StoredAction extends Action {

    private String senderId;
    private String fileId;
    private int chunkNo;

    public StoredAction(String senderId, String fileId, int chunkNo) {
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void process() {
        Chunk chunk = new Chunk(chunkNo);
        String chunkKey = chunk.buildChunkKey(fileId);
        Peer.getDataContainer().incStoredCurrRepDegree(chunkKey, senderId);
        Peer.getDataContainer().incBackedUpChunkCurrRepDegree(chunkKey);
    }
}
