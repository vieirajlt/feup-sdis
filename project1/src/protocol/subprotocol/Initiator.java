package protocol.subprotocol;

import app.TestApp;
import protocol.Chunk;
import protocol.Peer;
import protocol.SplitFile;

import java.nio.charset.StandardCharsets;

public class Initiator {

    public final static char CR  = (char) 0x0D;
    public final static char LF  = (char) 0x0A;

    private final static int MSG_CONFIG_PUTCHUNK = 0b111111;
    private final static int MSG_CONFIG_STORED = 0b001111;

    private final static String PUTCHUNK = "PUTCHUNK";
    private final static String STORED = "STORED";

    private final static int SLEEP_TIME_MS = 200;

    private static Initiator instance = null;

    private Initiator() {
    }

    public static Initiator getInstance() {
        if(instance == null) {
            instance = new Initiator();
        }
        return instance;
    }

    public boolean run(String message) {
        String[] cmd = message.split(" ");

        if(cmd[0].equals(TestApp.BACKUP)) {
            backup(cmd);
        } else if(cmd[0].equals(TestApp.RESTORE)) {
            restore(cmd);
        } else if(cmd[0].equals(TestApp.DELETE)) {
            delete(cmd);
        } else if(cmd[0].equals(TestApp.RECLAIM)) {
            reclaim(cmd);
        } else if(cmd[0].equals(TestApp.STATE)) {
            state(cmd);
        } else {
            return false;
        }
        return true;
    }

    private synchronized void backup(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.backup");
        String filepath = cmd[1];
        int repDegree = Integer.parseInt(cmd[2]);

        SplitFile sf = new SplitFile(filepath, repDegree);

        for (Chunk chunk: sf.getChunks()) {

            int chunkNo = chunk.getChunkNo();
            String body =  new String(chunk.getBody(), StandardCharsets.UTF_8);

            String message = buildMessage(cmd, MSG_CONFIG_PUTCHUNK, chunkNo, body);

            Peer.getBackupChannel().write(message);

            try {
                Thread.sleep(SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private synchronized void restore(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.restore");
    }

    private synchronized void delete(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.delete");
    }

    private synchronized void reclaim(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.reclaim");
    }

    private synchronized void state(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.state");
    }

    private String buildHeader(String[] cmd, int configuration, int chunkNo) {

        String message = cmd[0];
        int config = configuration;

        //protocol Version
        if(config % 2 == 1) {
            message += " " + Peer.getProtocolVersion();
        }
        config >>>= 1;

        //Server ID
        if(config % 2 == 1) {
            message += " " + Peer.getServerId();
        }
        config >>>= 1;

        //File ID
        if(config % 2 == 1) {
            message += " " + cmd[1];
        }
        config >>>= 1;

        //ChunkNo
        if(config % 2 == 1) {
            message += " " + chunkNo;
        }
        config >>>= 1;

        //Replication Degree
        if(config % 2 == 1) {
            message += " " + cmd[2];
        }

        //End Header
        message += " " + CR + LF + CR + LF;
        return message;
    }

    private String buildMessage(String[] cmd, int configuration, int chunkNo, String body) {
        String message = buildHeader(cmd, configuration, chunkNo);
        int config = configuration >>> 5;
        if(config % 2 == 1) {
            message += body;
        }
        return message;
    }
}