package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.handler.Handler;
import protocol.subprotocol.handler.PutchunkHandler;

import java.util.concurrent.TimeUnit;

public class RemovedAction extends Action implements Runnable {

    private String senderId;
    private String fileId;
    private String chunkKey;
    private int chunkNo;
    private boolean enhanced;

    public RemovedAction(String senderId, String fileId, String chunkKey, int chunkNo, boolean enhanced) {
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkKey = chunkKey;
        this.chunkNo = chunkNo;
        this.enhanced = enhanced;
    }

    @Override
    public void process() {
        //own files
        Peer.getDataContainer().decStoredCurrRepDegree(chunkKey, senderId);
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

            PutchunkHandler putchunkHandler = new PutchunkHandler(chunk, fileId, replicationDegree, enhanced);
            Peer.getExecutor().execute(putchunkHandler);
        }
    }
}
