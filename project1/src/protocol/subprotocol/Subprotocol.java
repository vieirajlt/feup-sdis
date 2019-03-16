package protocol.subprotocol;

import protocol.Peer;

public abstract class Subprotocol {

    protected final static char CR  = (char) 0x0D;
    protected final static char LF  = (char) 0x0A;

    protected final static int MSG_CONFIG_PUTCHUNK = 0b111111;

    protected final static int MSG_CONFIG_STORED = 0b001111;

    protected final static String PUTCHUNK = "PUTCHUNK";
    protected final static String GETCHUNK = "GETCHUNK";
    protected final static String REMOVED = "REMOVED";
    protected final static String DELETE = "DELETE";

    protected final static String STORED = "STORED";

    private final static int MAX_TIME_SLEEP_MS = 400;

    private int sleep_time_ms;

    protected Subprotocol() {
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

    public abstract boolean run(String message);

    protected abstract void backup(String[] cmd);

    protected abstract void restore(String[] cmd);

    protected abstract void delete(String[] cmd);

    protected abstract void reclaim(String[] cmd);

    public int getSleep_time_ms() {
        return sleep_time_ms;
    }
}
