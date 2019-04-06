package protocol.subprotocol.handler;


import protocol.Chunk;
import protocol.Peer;
import protocol.info.ChunkInfo;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        System.out.println(Peer.getDataContainer().getCurrStorageAmount() + " <= " + Peer.getDataContainer().getStorageCapacity());
        String chunkId, fileId, pathname = Chunk.getPathname();
        if (Peer.getDataContainer().getCurrStorageAmount() <= Peer.getDataContainer().getStorageCapacity()) {
            Runnable runnable = () -> deleteEmptyDirs(pathname);
            Peer.getExecutor().schedule(runnable, REMOVE_INBETWEEN_TIME_MS, TimeUnit.MILLISECONDS);
            return;
        }

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
        Peer.getDataContainer().setBackedUpChunkOnPeer(chunkKey, false);
        byte[] message = buildMessage(REMOVED, MSG_CONFIG_REMOVED, chunkInfo.getFileId(), chunkInfo.getChunkNo(), -1, null);

        Peer.getControlChannel().write(message);

        Peer.getExecutor().execute(this);

    }

    //TODO understand why this shit is not working
    private void deleteEmptyDirs(String pathname) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(pathname))) {
            for (Path path : directoryStream) {
                if (Files.isDirectory(path)) {
                    System.out.print(path.toString());
                    try {
                        Files.delete(path);
                        System.out.println(" empty");
                    } catch (DirectoryNotEmptyException ex) {
                        System.out.println(" not empty");
                    }
                }
            }
        } catch (IOException ex) {
        }
    }
}


