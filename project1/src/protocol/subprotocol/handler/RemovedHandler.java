package protocol.subprotocol.handler;


import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.FileManagement.ChunkInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static protocol.subprotocol.Subprotocol.REMOVED;

public class RemovedHandler extends Handler implements Runnable {

    private static final int REMOVED_INBETWEEN_TIME_MS = 1000;

    private int maxDiskSpace;
    public RemovedHandler(int maxDiskSpace) {
        this.maxDiskSpace = maxDiskSpace;
    }


    @Override
    public void run() {
        System.out.println("protocol.subprotocol.handler.RemovedHandler.run");
        Peer.getDataContainer().setStorageCapacity(maxDiskSpace);

        File folder = new File(Chunk.STORE_PATH + Peer.getServerId());
        File[] listOfFiles = folder.listFiles();

        String  chunkId, pathname = Chunk.STORE_PATH + Peer.getServerId() + "/";
        File chunkFile;
        ChunkInfo chunkInfo;


       List sortedBackedUpChunks = Peer.getDataContainer().getBackedUpChunksSortedInfo();
       for(int i = 0; i < sortedBackedUpChunks.size(); i++)
       {
           if(Peer.getDataContainer().getCurrStorageAmount() <= Peer.getDataContainer().getStorageCapacity() )
               return;

           chunkInfo = (ChunkInfo) sortedBackedUpChunks.get(i);
           chunkId = chunkInfo.getChunkId();
           chunkFile = new File(pathname + chunkId + ".ser");

           Peer.getDataContainer().decCurrStorageAmount( (double) chunkFile.length()/1000.0);

           System.out.println(Peer.getDataContainer().getCurrStorageAmount());

           if (!chunkFile.delete())
               continue;


           Peer.getDataContainer().deleteBackedUpChunk(chunkId);
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


