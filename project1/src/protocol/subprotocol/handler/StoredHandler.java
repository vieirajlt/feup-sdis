package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.fileManagement.FileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.STORED;

public class StoredHandler extends Handler implements Runnable {

    private int senderId;
    private String fileId;
    private Chunk chunk;
    private int repDegree;
    private Float maxCompletionPercentage;
    private byte[] message;

    public StoredHandler(int senderId, String fileId, Chunk chunk, int repDegree, float maxCompletionPercentage) {
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunk = chunk;
        this.repDegree = repDegree;
        this.maxCompletionPercentage = maxCompletionPercentage;
    }

    @Override
    public void handle() {

        //case it is Peer's own file
        if (Peer.getDataContainer().getOwnFile(fileId) != null)
            return;

        String chunkKey = chunk.buildChunkKey(fileId);
        message = buildMessage(STORED, MSG_CONFIG_STORED, fileId, chunk.getChunkNo(), -1, (byte[]) null);

        // case already backed up
        if (Peer.getDataContainer().isBackedUpChunkOnPeer(chunkKey)) {
            return;
        }

        //add even if not saved on Peer for other peers info collection
        int chunkSize = chunk.getSize();
        Peer.getDataContainer().addBackedUpChunk(chunkKey, senderId, repDegree, chunkSize);

        if(Peer.getDataContainer().isBackedUpChunkInfoHandling(chunkKey)) {
            //Peer.getDataContainer().setBackedUpChunksChunkInfoHandling(chunkKey,false);
            return;
        }

        if(Peer.getDataContainer().getCompletionPercentage() > maxCompletionPercentage)
            return;

        Peer.getDataContainer().setBackedUpChunksChunkInfoHandling(chunkKey,true);


        Peer.getExecutor().schedule(this, getSleep_time_ms(), TimeUnit.MILLISECONDS);

    }

    @Override
    public void run() {

        String chunkKey = chunk.buildChunkKey(fileId);

        if(Peer.getDataContainer().getCurrStorageAmount() + FileManager.MAX_CHUNK_SIZE > Peer.getDataContainer().getStorageCapacity()){
            Peer.getDataContainer().setBackedUpChunksChunkInfoHandling(chunkKey,false);
            return;
        }

        // case not enough space to store
        // OR repDegree exceeded
        if (Peer.getDataContainer().getBackedUpChunkCurrRepDegree(chunkKey) >= repDegree) {
            Peer.getDataContainer().setBackedUpChunksChunkInfoHandling(chunkKey,false);
            return;
        }

        chunk.store(fileId);
        if(!chunk.isLoaded())
        {
            Peer.getDataContainer().setBackedUpChunksChunkInfoHandling(chunkKey,false);
            return;
        }


        if(!Peer.getDataContainer().incCurrStorageAmountAndCheckSpace(chunk.getSize())) {
            chunk.delete(fileId);
            Peer.getDataContainer().setBackedUpChunksChunkInfoHandling(chunkKey,false);
            return;
        }

        Peer.getDataContainer().addPeerChunk(chunkKey);
        Peer.getDataContainer().setBackedUpChunkOnPeer(chunkKey, true);
        Peer.getDataContainer().incBackedUpChunkCurrRepDegree(chunkKey);
        Peer.getDataContainer().setBackedUpChunksChunkInfoHandling(chunkKey,false);
        Peer.getControlChannel().write(message);
    }
}
