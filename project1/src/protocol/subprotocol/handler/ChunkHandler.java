package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.Chunk;

import java.nio.charset.StandardCharsets;

import static protocol.subprotocol.Subprotocol.CHUNK;

public class ChunkHandler extends Handler implements Runnable {

    private String fileId;
    private int chunkNo;

    public ChunkHandler(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        System.out.println("protocol.subprotocol.handler.senchunk.run");
        Chunk chunk = new Chunk(chunkNo);
        Chunk loaded = chunk.load(fileId, chunkNo);
        if (loaded != null) {
            String chunkId = Chunk.buildChunkId(fileId,chunkNo);
            Peer.getDataContainer().setPeerChunk(chunkId,false );
            try {
                Thread.sleep(getSleep_time_ms());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!Peer.getDataContainer().getChunkShippingState(chunkId))
            {
                String body = new String(loaded.getBody(), StandardCharsets.UTF_8);
                String message = buildMessage(CHUNK, MSG_CONFIG_SENDCHUNK, fileId, chunkNo, -1, body);
                // System.out.println(message);
                Peer.getRestoreChannel().write(message);
                System.out.println("send message...");
                Peer.getDataContainer().setPeerChunk(chunkId,true);
            }
        }
    }
}
