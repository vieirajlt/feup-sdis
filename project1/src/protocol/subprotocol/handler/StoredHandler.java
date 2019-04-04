package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;

import java.io.File;

import static protocol.subprotocol.Subprotocol.STORED;

public class StoredHandler extends Handler implements Runnable {

    private String fileId;
    private Chunk chunk;
    private int repDegree;

    public StoredHandler(String fileId, Chunk chunk, int repDegree) {
        this.fileId = fileId;
        this.chunk = chunk;
        this.repDegree = repDegree;
    }

    @Override
    public void run() {

        String chunkId = Chunk.buildChunkKey(fileId, chunk.getChunkNo());
        byte[] message = buildMessage(STORED, MSG_CONFIG_STORED, fileId, chunk.getChunkNo(), -1, null);

        //add even if not saved on Peer for other peers info collection
        Peer.getDataContainer().addBackedUpChunk(chunkId, repDegree);

        // case already backed up
        if (Peer.getDataContainer().isBackedUpChunkOnPeer(chunkId)) {
            return;
        }

        try {
            Thread.sleep(getSleep_time_ms());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        chunk.store(fileId);

        File chunkFile = new File(chunk.getChunkFolderPath(fileId) + chunk.buildChunkFileId(chunk.getChunkNo()));

        // case not enough space to store
        // OR repDegree exceeded
        if (!Peer.getDataContainer().incCurrStorageAmount(chunkFile.length()) ||
                Peer.getDataContainer().getBackedUpChunkCurrRepDegree(chunkId) >= repDegree) {
            chunkFile.delete();
            File dir = new File(chunk.getChunkFolderPath(fileId));
            if(dir.isDirectory() && dir.list().length == 0) {
                dir.delete();
            }
            return;
        }

        Peer.getControlChannel().write(message);
        Peer.getDataContainer().addPeerChunk(chunkId);
        Peer.getDataContainer().setBackedUpChunkOnPeer(chunkId, true);
        Peer.getDataContainer().incBackedUpChunkCurrRepDegree(chunkId);
    }
}
