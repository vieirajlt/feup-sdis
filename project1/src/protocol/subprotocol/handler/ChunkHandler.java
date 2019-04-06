package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.Chunk;

import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.CHUNK;

public class ChunkHandler extends Handler implements Runnable {

    private String fileId;
    private int chunkNo;
    private String chunkKey;
    private Chunk chunk;

    public ChunkHandler(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        chunk = new Chunk(chunkNo);
        chunk.load(fileId);
    }

    @Override
    public void handle() {
        System.out.println("protocol.subprotocol.handler.senchunk.run");

        chunkKey = chunk.buildChunkKey(fileId);
        Peer.getDataContainer().setPeerChunk(chunkKey, false);
        Peer.getExecutor().schedule(this, getSleep_time_ms(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        while(!chunk.isLoaded()); //TODO this is a precaution... don't know if really  needed
        if (!Peer.getDataContainer().getChunkShippingState(chunkKey)) {
            byte[] body = chunk.getBody();
            byte[] message = buildMessage(CHUNK, MSG_CONFIG_SENDCHUNK, fileId, chunkNo, -1, body);
            // System.out.println(message);
            Peer.getRestoreChannel().write(message);

            Peer.getDataContainer().setPeerChunk(chunkKey, true);
        }
    }
}
