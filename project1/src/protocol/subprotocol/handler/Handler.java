package protocol.subprotocol.handler;

import protocol.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Handler {

    public final static char CR = (char) 0x0D;
    public final static char LF = (char) 0x0A;

    protected final static int MSG_CONFIG_PUTCHUNK = 0b111111;

    protected final static int MSG_CONFIG_STORED = 0b001111;

    protected final static int MSG_CONFIG_SENDCHUNK = 0b101111;

    protected final static int MSG_CONFIG_GETCHUNK = 0b001111;

    private final static int MAX_TIME_SLEEP_MS = 400;

    private int sleep_time_ms;

    public Handler() {
        sleep_time_ms = (int)(Math.random() * (MAX_TIME_SLEEP_MS + 1));
    }

    private String buildHeader(String type, int configuration, String fileId, int chunkNo, int replicationDegree) {

        String message = type;
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
            message += " " + fileId;
        }
        config >>>= 1;

        //ChunkNo
        if(config % 2 == 1) {
            message += " " + chunkNo;
        }
        config >>>= 1;

        //Replication Degree
        if(config % 2 == 1) {
            message += " " + replicationDegree;
        }

        //End Header
        message += " " + CR + LF + CR + LF;
        return message;
    }

    protected String buildMessage(String type, int configuration, String fileId, int chunkNo, int replicationDegree, String body) {
        String message = buildHeader(type, configuration, fileId, chunkNo, replicationDegree);
        int config = configuration >>> 5;
        if(config % 2 == 1) {
            message += body;
        }
        return message;
    }

    public int getSleep_time_ms() {
        return sleep_time_ms;
    }
}
