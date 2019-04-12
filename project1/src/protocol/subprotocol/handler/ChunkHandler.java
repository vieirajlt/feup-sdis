package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.communication.tcp.Server;

import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.CHUNK;

public class ChunkHandler extends Handler implements Runnable {

    private String fileId;
    private int chunkNo;
    private String chunkKey;
    private Chunk chunk;
    private boolean enhanced;

    public ChunkHandler(String fileId, int chunkNo, boolean enhanced) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        chunk = new Chunk(chunkNo);
        this.enhanced = enhanced;
    }

    @Override
    public void handle() {
        System.out.println("protocol.subprotocol.handler.senchunk.run");

        chunkKey = chunk.buildChunkKey(fileId);

        //case peer does not store the chunk...
        if(!Peer.getDataContainer().isBackedUpChunkOnPeer(chunkKey))
            return;

        chunk.load(fileId);
        Peer.getDataContainer().setPeerChunk(chunkKey, false);
        Peer.getExecutor().schedule(this, getSleep_time_ms(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        while(!chunk.isLoaded()); //TODO this is a precaution... don't know if really  needed
        if (!Peer.getDataContainer().getChunkShippingState(chunkKey)) {
            byte[] body = chunk.getBody();

            byte[] message;
            if(enhanced) {
                Server tcpServer = new Server(chunk);
                message = buildMessage(CHUNK, MSG_CONFIG_SENDCHUNK, fileId, chunkNo, -1, tcpServer.getConnectionSettings());
                Peer.getRestoreChannel().write(message);
                tcpServer.sendChunk();
            } else {
                message = buildMessage(CHUNK, MSG_CONFIG_SENDCHUNK, fileId, chunkNo, -1, body);
                Peer.getRestoreChannel().write(message);
            }

            Peer.getDataContainer().setPeerChunk(chunkKey, true);
        }
    }
}
