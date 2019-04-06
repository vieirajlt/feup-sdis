package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;

import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.PUTCHUNK;

public class PutchunkHandler extends Handler implements Runnable {

    private static final int MAX_PUTCHUNK_REPEAT = 5;
    private static final int PUTCHUNK_INBETWEEN_TIME_MS = 1000;

    private int repeatCnt;
    private Chunk chunk;
    private String fileId;
    private int repDegree;

    public PutchunkHandler(Chunk chunk, String fileId, int repDegree) {
        this.chunk = chunk;
        this.fileId = fileId;
        this.repDegree = repDegree;
        this.repeatCnt = 0;
    }

    @Override
    public void run() {
        handle();
    }

    @Override
    public void handle() {
        if (repeatCnt < MAX_PUTCHUNK_REPEAT) {

            int chunkNo = chunk.getChunkNo();
            String chunkId = fileId + "_" + chunkNo;
            System.out.println("protocol.subprotocol.handler.PutchunkHandler.run -> repeat number: " + repeatCnt + " Chunk: " + chunk.getChunkNo() +
                    "\n\tcurr: " + Peer.getDataContainer().getStoredCurrRepDegree(chunkId) + " " +
                    Peer.getDataContainer().getBackedUpChunkCurrRepDegree(chunkId) + " repDegree: " + repDegree);
            if (Peer.getDataContainer().getStoredCurrRepDegree(chunkId) >= repDegree ||
                    Peer.getDataContainer().getBackedUpChunkCurrRepDegree(chunkId) >= repDegree)
                return;

            byte[] body = chunk.getBody();

            byte[] message = buildMessage(PUTCHUNK, MSG_CONFIG_PUTCHUNK, fileId, chunkNo, repDegree, body);

            Peer.getBackupChannel().write(message);

            ++repeatCnt;

            //delay for stored msg receiving
            Peer.getExecutor().schedule(this, PUTCHUNK_INBETWEEN_TIME_MS, TimeUnit.MILLISECONDS);
        }
    }
}
