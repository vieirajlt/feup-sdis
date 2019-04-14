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
    private boolean enhanced;

    public PutchunkHandler(Chunk chunk, String fileId, int repDegree, boolean enhanced) {
        this.chunk = chunk;
        this.fileId = fileId;
        this.repDegree = repDegree;
        this.repeatCnt = 0;
        this.enhanced = enhanced;
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
            if (Peer.getDataContainer().getStoredCurrRepDegree(chunkId) >= repDegree ||
                    Peer.getDataContainer().getBackedUpChunkCurrRepDegree(chunkId) >= repDegree)
                return;

            byte[] body = chunk.getBody();

            byte[] message;
            if(enhanced)
                message = buildMessage(PUTCHUNK, MSG_CONFIG_PUTCHUNK, fileId, chunkNo, repDegree, body, completionPercentage());
            else
                message = buildMessage(PUTCHUNK, MSG_CONFIG_PUTCHUNK, fileId, chunkNo, repDegree, body);

            Peer.getBackupChannel().write(message);

            ++repeatCnt;

            //delay for stored msg receiving
            Peer.getExecutor().schedule(this, PUTCHUNK_INBETWEEN_TIME_MS, TimeUnit.MILLISECONDS);
        }
    }

    private float completionPercentage() {
        float percentage = 0;
        switch (repeatCnt) {
            case 0:
                percentage = (float) 0.6;
                break;
            case 1:
                percentage = (float) 0.75;
                break;
            case 2:
                percentage = (float) 0.85;
                break;
            case 3:
                percentage = (float) 1.0;
                break;
            case 4:
                percentage = (float) 1.0;
                break;
            default:
                percentage = (float) 1.0;
                break;
        }
        return percentage;
    }
}
