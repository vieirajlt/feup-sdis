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
        System.out.println("protocol.subprotocol.handler.RemovedHandler.run");
        Peer.getDataContainer().setStorageCapacity(maxDiskSpace * 1000);

        Peer.getExecutor().execute(this);

    }

    @Override
    public void run() {
        if (Peer.getDataContainer().getCurrStorageAmount() <= Peer.getDataContainer().getStorageCapacity()) {
            return;
        }

        String chunkId, fileId, pathname = Chunk.getPathname();
        ChunkInfo chunkInfo;

        List sortedBackedUpChunks = Peer.getDataContainer().getBackedUpChunksOnPeerSortedInfo();

        if (sortedBackedUpChunks.size() < 1)
            return;

        chunkInfo = (ChunkInfo) sortedBackedUpChunks.get(0);
        Chunk chunk = new Chunk(chunkInfo.getChunkNo());
        chunkId = chunk.buildChunkId();
        fileId = chunkInfo.getFileId();

        Path path = Paths.get(pathname + fileId + "/" + chunkId);
        long length;

        try {
            length = Files.size(path);
            chunk.delete(fileId);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error Deleting...");
            return;
        }

        Peer.getDataContainer().decCurrStorageAmount(length);

        String chunkKey = chunk.buildChunkKey(fileId);
        Peer.getDataContainer().deleteBackedUpChunk(chunkKey);
        //Peer.getDataContainer().setBackedUpChunkOnPeer(chunkKey, false);
        byte[] message = buildMessage(REMOVED, MSG_CONFIG_REMOVED, chunkInfo.getFileId(), chunkInfo.getChunkNo(), -1, null);

        Peer.getControlChannel().write(message);

        Peer.getExecutor().execute(this);

    }
}


