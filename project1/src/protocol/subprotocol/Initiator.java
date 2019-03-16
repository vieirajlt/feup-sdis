package protocol.subprotocol;

import app.TestApp;
import protocol.Chunk;
import protocol.Peer;
import protocol.SplitFile;

import java.nio.charset.StandardCharsets;

public class Initiator extends Subprotocol {

    private static final int MAX_PUTCHUNK_REPEAT = 5;

    public Initiator() {

    }

    @Override
    public boolean run(String message) {
        String[] cmd = message.split(" ");

        if (cmd[0].equals(TestApp.BACKUP)) {
            backup(cmd);
        } else if (cmd[0].equals(TestApp.RESTORE)) {
            restore(cmd);
        } else if (cmd[0].equals(TestApp.DELETE)) {
            delete(cmd);
        } else if (cmd[0].equals(TestApp.RECLAIM)) {
            reclaim(cmd);
        } else if (cmd[0].equals(TestApp.STATE)) {
            state(cmd);
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected void backup(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.backup");
            String filepath = cmd[1];
            int repDegree = Integer.parseInt(cmd[2]);

            SplitFile sf = new SplitFile(filepath, repDegree);

            int repeatCnt = 0;
            boolean repDone = false;

            while(repeatCnt < MAX_PUTCHUNK_REPEAT && !repDone) {
                System.out.println("protocol.subprotocol.Initiator.backup -> PUTCHUNK repeat number: " + repeatCnt);
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
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void restore(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.restore");
        }
    }

    @Override
    protected void delete(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.delete");
        }
    }

    @Override
    protected void reclaim(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.reclaim");
        }
    }

    private synchronized void state(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.state");
        }
    }


}
