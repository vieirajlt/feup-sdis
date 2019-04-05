package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.handler.Handler;
import protocol.subprotocol.handler.PutchunkHandler;

public class RemovedAction extends Action {

    private String fileId;
    private String chunkKey;
    private int chunkNo;

    public RemovedAction(String fileId, String chunkKey, int chunkNo) {
        this.fileId = fileId;
        this.chunkKey = chunkKey;
        this.chunkNo = chunkNo;
    }

    @Override
    public void process() {
        Peer.getDataContainer().decBackedUpChunkCurrRepDegree(chunkKey);

        //if the peer does not store the chunk
        if (!Peer.getDataContainer().isBackedUpChunkOnPeer(chunkKey))
            return;

        //random delay uniformly distributed between 0 and 400 ms
        try {
            Thread.sleep(Handler.buildSleep_time_ms());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //if no putchunk message was received send one
        if (Peer.getDataContainer().getDifferenceBtCurrDesiredRepDegrees(chunkKey) < 0) {

            Chunk chunk = new Chunk(chunkNo);
            int replicationDegree = Peer.getDataContainer().getBackedUpChunkDesiredRepDegree(chunkKey);

            PutchunkHandler putchunkHandler = new PutchunkHandler(chunk.load(fileId, chunkNo), fileId, replicationDegree);
            Peer.getExecutor().execute(putchunkHandler);
        }
    }
}
