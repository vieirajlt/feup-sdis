package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.STORED;

public class StoredHandler extends Handler implements Runnable {

    private String fileId;
    private Chunk chunk;
    private int repDegree;
    private byte[] message;

    public StoredHandler(String fileId, Chunk chunk, int repDegree) {
        this.fileId = fileId;
        this.chunk = chunk;
        this.repDegree = repDegree;
    }

    @Override
    public void handle() {

        //case it is Peer's own file
        if (Peer.getDataContainer().getOwnFile(fileId) != null)
            return;

        String chunkKey = chunk.buildChunkKey(fileId);
        message = buildMessage(STORED, MSG_CONFIG_STORED, fileId, chunk.getChunkNo(), -1, null);

        //add even if not saved on Peer for other peers info collection
        Peer.getDataContainer().addBackedUpChunk(chunkKey, repDegree);

        // case already backed up
        if (Peer.getDataContainer().isBackedUpChunkOnPeer(chunkKey)) {
            return;
        }

        Peer.getExecutor().schedule(this, getSleep_time_ms(), TimeUnit.MILLISECONDS);

    }

    @Override
    public void run() {
        chunk.store(fileId);

        // case not enough space to store
        // OR repDegree exceeded
        String chunkKey = chunk.buildChunkKey(fileId);
        if (Peer.getDataContainer().getCurrStorageAmount() > Peer.getDataContainer().getStorageCapacity() ||
                Peer.getDataContainer().getBackedUpChunkCurrRepDegree(chunkKey) >= repDegree) {
            chunk.delete(fileId);
            return;
        }

        Path path = Paths.get(Chunk.getPathname() + fileId + "/" + chunk.buildChunkId());
        try {
            Peer.getDataContainer().incCurrStorageAmount(Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Peer.getControlChannel().write(message);
        Peer.getDataContainer().addPeerChunk(chunkKey);
        Peer.getDataContainer().setBackedUpChunkOnPeer(chunkKey, true);
        Peer.getDataContainer().incBackedUpChunkCurrRepDegree(chunkKey);
    }
}
