package protocol.subprotocol.handler;


import protocol.Chunk;
import protocol.Peer;

import java.io.File;
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

        String  chunkId;

        //delete all the chunks from the file
        for (int i = 0; i < listOfFiles.length; i++) {
            if(Peer.getDataContainer().getCurrStorageAmount() <= Peer.getDataContainer().getStorageCapacity() )
                return;
            if (listOfFiles[i].isFile()) {
                chunkId = listOfFiles[i].getName().split(".ser")[0];
                int difference = Peer.getDataContainer().getDifferenceBtCurrDesiredRepDegrees(chunkId);
                System.out.println(difference);
                if( difference > 0) {
                    //delete file
                    listOfFiles[i].delete();
                    Peer.getDataContainer().deleteBackedUpChunk(chunkId);
                    byte[] message = buildMessage(REMOVED, MSG_CONFIG_REMOVED, chunkId.split("_")[0], -1, -1, null);

                    try {
                        Thread.sleep(REMOVED_INBETWEEN_TIME_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Peer.getControlChannel().write(message);
                }
                /*else if (difference == 0){

                }*/
            }
        }
    }



}
