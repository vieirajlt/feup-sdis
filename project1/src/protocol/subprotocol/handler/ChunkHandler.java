package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.Chunk;

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
            String chunkId = Chunk.buildChunkKey(fileId,chunkNo);
            Peer.getDataContainer().setPeerChunk(chunkId,false );
            try {
                Thread.sleep(getSleep_time_ms());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!Peer.getDataContainer().getChunkShippingState(chunkId))
            {
                byte[] body = loaded.getBody();
                byte[] message = buildMessage(CHUNK, MSG_CONFIG_SENDCHUNK, fileId, chunkNo, -1, body);
                // System.out.println(message);
                Peer.getRestoreChannel().write(message);

                Peer.getDataContainer().setPeerChunk(chunkId,true);
            }
        }
    }
}
