package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.DataContainer;
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

        DataContainer dataContainer = Peer.getDataContainer();

        String chunkId = Chunk.buildChunkKey(fileId, chunk.getChunkNo());
        byte[] message = buildMessage(STORED, MSG_CONFIG_STORED, fileId, chunk.getChunkNo(), -1, null);

        //add even if not saved on Peer for other peers info collection
        dataContainer.addBackedUpChunk(chunkId, repDegree);

        // case already backed up
        if (dataContainer.isBackedUpChunkOnPeer(chunkId)) {
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
        if (dataContainer.getCurrStorageAmount() > dataContainer.getStorageCapacity() ||
                dataContainer.getBackedUpChunkCurrRepDegree(chunkId) >= repDegree) {
            chunkFile.delete();
            File dir = new File(chunk.getChunkFolderPath(fileId));
            if(dir.isDirectory() && dir.list().length == 0) {
                dir.delete();
            }
            return;
        }

        Peer.getControlChannel().write(message);
        dataContainer.addPeerChunk(chunkId);
        dataContainer.setBackedUpChunkOnPeer(chunkId, true);
        dataContainer.incBackedUpChunkCurrRepDegree(chunkId);
    }
}
