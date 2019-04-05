package protocol.subprotocol.handler;


import protocol.Chunk;
import protocol.Peer;
import protocol.ChunkInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.REMOVED;

public class RemovedHandler extends Handler implements Runnable {

    private static final int REMOVED_INBETWEEN_TIME_MS = 1000;

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
        if (Peer.getDataContainer().getCurrStorageAmount() <= Peer.getDataContainer().getStorageCapacity())
            return;

        String chunkId, fileId, pathname = Chunk.getPathname();
        File chunkFile;
        ChunkInfo chunkInfo;

        List sortedBackedUpChunks = Peer.getDataContainer().getBackedUpChunksSortedInfo();

        if(sortedBackedUpChunks.size() < 1)
            return;

        chunkInfo = (ChunkInfo) sortedBackedUpChunks.get(0);
        chunkId = Chunk.buildChunkFileId(chunkInfo.getChunkNo());
        fileId = chunkInfo.getFileId();
        chunkFile = new File(pathname + fileId + "/" + chunkId);

        if (!chunkFile.delete()) {
            System.out.println("Error Deleting...");
            return;
        }

        System.out.println(Peer.getDataContainer().getCurrStorageAmount());
        String chunkKey = Chunk.buildChunkKey(fileId, chunkInfo.getChunkNo());
        Peer.getDataContainer().setBackedUpChunkOnPeer(chunkKey, false);
        byte[] message = buildMessage(REMOVED, MSG_CONFIG_REMOVED, chunkInfo.getFileId(), chunkInfo.getChunkNo(), -1, null);

        Peer.getControlChannel().write(message);

        Peer.getExecutor().schedule(this, REMOVED_INBETWEEN_TIME_MS, TimeUnit.MILLISECONDS);

    }
}


