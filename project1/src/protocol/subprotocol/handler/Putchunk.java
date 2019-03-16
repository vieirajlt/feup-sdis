package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;
import protocol.SplitFile;

import java.nio.charset.StandardCharsets;

import static protocol.subprotocol.Subprotocol.PUTCHUNK;

public class Putchunk extends Handler implements Runnable{

    private static final int MAX_PUTCHUNK_REPEAT = 5;
    private static final int PUTCHUNK_INBETWEEN_TIME_MS = 1000;

    private int repeatCnt = 0;
    private boolean repDone = false;
    private SplitFile sf;

    public Putchunk(SplitFile sf) {
        this.sf = sf;
    }

    @Override
    public void run() {
        while(repeatCnt < MAX_PUTCHUNK_REPEAT && !repDone) {
            System.out.println("protocol.subprotocol.handler.Putchunk.run -> repeat number: " + repeatCnt);
            repDone = true;

            for (Chunk chunk : sf.getChunks()) {

                int chunkNo = chunk.getChunkNo();
                String chunkId = sf.getFileId() + "_" + chunkNo;
                if(Peer.getDataContainer().getCurrRepDegree(chunkId) >= sf.getReplicationDegree())
                    continue;

                repDone = false;
                String body = new String(chunk.getBody(), StandardCharsets.UTF_8);

                String message = buildMessage(PUTCHUNK, MSG_CONFIG_PUTCHUNK, sf.getFileId(), chunkNo, sf.getReplicationDegree(), body);

                Peer.getBackupChannel().write(message);

            }
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
