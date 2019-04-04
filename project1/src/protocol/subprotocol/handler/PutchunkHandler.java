package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.FileManagement.SplitFile;

import static protocol.subprotocol.Subprotocol.PUTCHUNK;

public class PutchunkHandler extends Handler implements Runnable {

    private static final int MAX_PUTCHUNK_REPEAT = 5;
    private static final int PUTCHUNK_INBETWEEN_TIME_MS = 1000;

    private int repeatCnt;
    private boolean repDone;
    private Chunk chunk;
    private String fileId;
    private int repDegree;

    public PutchunkHandler(Chunk chunk, String fileId, int repDegree) {
        this.chunk = chunk;
        this.fileId = fileId;
        this.repDegree = repDegree;
        repeatCnt = 0;
        repDone = false;
    }

    public PutchunkHandler(Chunk chunk, SplitFile sf) {
        this.chunk = chunk;
        this.fileId = sf.getFileId();
        this.repDegree = sf.getReplicationDegree();
        repeatCnt = 0;
        repDone = false;
    }

    @Override
    public void run() {
        while (repeatCnt < MAX_PUTCHUNK_REPEAT && !repDone) {
            repDone = true;

            int chunkNo = chunk.getChunkNo();
            String chunkId = fileId + "_" + chunkNo;
            System.out.println("protocol.subprotocol.handler.PutchunkHandler.run -> repeat number: " + repeatCnt + " Chunk: " + chunk.getChunkNo() +
                    "\n\tcurr: " + Peer.getDataContainer().getStoredCurrRepDegree(chunkId) + " repDegree: " + repDegree);
            if (Peer.getDataContainer().getStoredCurrRepDegree(chunkId) >= repDegree)
                continue;


            repDone = false;
            byte[] body = chunk.getBody();

            byte[] message = buildMessage(PUTCHUNK, MSG_CONFIG_PUTCHUNK, fileId, chunkNo, repDegree, body);

            Peer.getBackupChannel().write(message);

            ++repeatCnt;

            //delay for stored msg receiving
            try {
                Thread.sleep(PUTCHUNK_INBETWEEN_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
