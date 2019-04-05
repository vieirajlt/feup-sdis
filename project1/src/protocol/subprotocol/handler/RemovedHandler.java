package protocol.subprotocol.handler;


import protocol.Chunk;
import protocol.Peer;
import protocol.ChunkInfo;

import java.io.File;
import java.util.List;

import static protocol.subprotocol.Subprotocol.REMOVED;

public class RemovedHandler extends Handler implements Runnable {

    private static final int REMOVED_INBETWEEN_TIME_MS = 1000;

    private long maxDiskSpace;

    public RemovedHandler(long maxDiskSpace) {
        this.maxDiskSpace = maxDiskSpace;
    }


    @Override
    public void run() {
        System.out.println("protocol.subprotocol.handler.RemovedHandler.run");
        Peer.getDataContainer().setStorageCapacity(maxDiskSpace * 1000);

        String chunkId, fileId, pathname = Chunk.getPathname();
        File chunkFile;
        ChunkInfo chunkInfo;


        List sortedBackedUpChunks = Peer.getDataContainer().getBackedUpChunksSortedInfo();
        for (int i = 0; i < sortedBackedUpChunks.size(); i++) {
            if (Peer.getDataContainer().getCurrStorageAmount() <= Peer.getDataContainer().getStorageCapacity())
                return;

            chunkInfo = (ChunkInfo) sortedBackedUpChunks.get(i);
            chunkId = Chunk.buildChunkFileId(chunkInfo.getChunkNo());
            fileId = chunkInfo.getFileId();
            chunkFile = new File(pathname + fileId + "/" + chunkId);

            long chunkFileLength = chunkFile.length();

            if (!chunkFile.delete())
                continue;

            System.out.println(Peer.getDataContainer().getCurrStorageAmount());
            String chunkKey = Chunk.buildChunkKey(fileId, chunkInfo.getChunkNo());
            Peer.getDataContainer().setBackedUpChunkOnPeer(chunkKey, false);
            byte[] message = buildMessage(REMOVED, MSG_CONFIG_REMOVED, chunkInfo.getFileId(), chunkInfo.getChunkNo(), -1, null);

            try {
                Thread.sleep(REMOVED_INBETWEEN_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Peer.getControlChannel().write(message);
        }
    }

}


