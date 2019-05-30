package protocol.subprotocol.handler;


import protocol.Chunk;
import protocol.Peer;
import protocol.info.ChunkInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static protocol.subprotocol.Subprotocol.REMOVED;

public class RemovedHandler extends Handler implements Runnable {

    private static final int REMOVE_INBETWEEN_TIME_MS = 1000;

    private long maxDiskSpace;

    public RemovedHandler(long maxDiskSpace) {
        this.maxDiskSpace = maxDiskSpace;
    }

    @Override
    public void handle() {
        Peer.getDataContainer().setStorageCapacity(maxDiskSpace * 1000);

        Peer.getExecutor().execute(this);

    }

    @Override
    public void run() {
        if (Peer.getDataContainer().getCurrStorageAmount() <= Peer.getDataContainer().getStorageCapacity()) {
            return;
        }

        String chunkId, fileId, pathname = Chunk.getChunkFolderPath();
        ChunkInfo chunkInfo;

        List sortedBackedUpChunks = Peer.getDataContainer().getBackedUpChunksOnPeerSortedInfo();

        if (sortedBackedUpChunks.size() < 1)
            return;

        chunkInfo = (ChunkInfo) sortedBackedUpChunks.get(0);
        Chunk chunk = new Chunk(chunkInfo.getChunkNo());
        chunkId = chunk.buildChunkId();
        fileId = chunkInfo.getFileId();
        int length = chunkInfo.getSize();

        chunk.delete(fileId);

        Peer.getDataContainer().decCurrStorageAmount(length);

        String chunkKey = chunk.buildChunkKey(fileId);
        Peer.getDataContainer().deleteBackedUpChunk(chunkKey);
        //Peer.getDataContainer().setBackedUpChunkOnPeer(chunkKey, false);
        byte[] message = buildMessage(REMOVED, MSG_CONFIG_REMOVED, chunkInfo.getFileId(), chunkInfo.getChunkNo(), -1,  (byte[]) null);

        Peer.getControlChannel().write(message);

        Peer.getExecutor().execute(this);

    }
}


