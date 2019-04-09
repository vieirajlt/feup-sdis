package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.handler.Handler;
import protocol.subprotocol.handler.PutchunkHandler;

import java.util.concurrent.TimeUnit;

public class RemovedAction extends Action implements Runnable {

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
        //own files
        Peer.getDataContainer().decStoredCurrRepDegree(chunkKey);
        //other files
        Peer.getDataContainer().decBackedUpChunkCurrRepDegree(chunkKey);

        //if the peer does not store the chunk
        if (!Peer.getDataContainer().isBackedUpChunkOnPeer(chunkKey))
            return;

        Peer.getExecutor().schedule(this, Handler.buildSleep_time_ms(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        //if no putchunk message was received send one
        if (Peer.getDataContainer().getDifferenceBtCurrDesiredRepDegrees(chunkKey) < 0) {

            Chunk chunk = new Chunk(chunkNo);
            chunk.load(fileId);
            int replicationDegree = Peer.getDataContainer().getBackedUpChunkDesiredRepDegree(chunkKey);

            PutchunkHandler putchunkHandler = new PutchunkHandler(chunk, fileId, replicationDegree);
            Peer.getExecutor().execute(putchunkHandler);
        }
    }
}
