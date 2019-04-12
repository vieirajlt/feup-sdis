package protocol.subprotocol.handler;

import protocol.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class Handler {

    public final static char CR = (char) 0x0D;
    public final static char LF = (char) 0x0A;

    protected final static int MSG_CONFIG_PUTCHUNK = 0b111111;

    protected final static int MSG_CONFIG_STORED = 0b001111;

    protected final static int MSG_CONFIG_SENDCHUNK = 0b101111;

    protected final static int MSG_CONFIG_GETCHUNK = 0b001111;

    protected final static int MSG_CONFIG_DELETE = 0b000111;

    protected final static int MSG_CONFIG_REMOVED = 0b001111;

    private final static int MAX_TIME_SLEEP_MS = 400;

    private int sleep_time_ms;

    public Handler() {
        sleep_time_ms = buildSleep_time_ms();
    }

    private byte[] buildHeader(String type, int configuration, String fileId, int chunkNo, int replicationDegree, float completionPercentage, String connection) {

        String message = type;
        int config = configuration;

        //protocol Version
        if (config % 2 == 1) {
            message += " " + Peer.getProtocolVersion();
        }
        config >>>= 1;

        //Server ID
        if (config % 2 == 1) {
            message += " " + Peer.getServerId();
        }
        config >>>= 1;

        //File ID
        if (config % 2 == 1) {
            message += " " + fileId;
        }
        config >>>= 1;

        //ChunkNo
        if (config % 2 == 1) {
            message += " " + chunkNo;
        }
        config >>>= 1;

        //Replication Degree
        if (config % 2 == 1) {
            message += " " + replicationDegree;
        }

        //backup enhancement
        if(completionPercentage > 0) {
            message += " " + completionPercentage;
        }

        //restore enhancement
        if(connection != null) {
            message += " " + connection;
        }

        //End Header
        message += " " + CR + LF + CR + LF;
        return message.getBytes(StandardCharsets.UTF_8);
    }

    protected byte[] buildMessage(String type, int configuration, String fileId, int chunkNo, int replicationDegree, byte[] body) {
        byte[] header = buildHeader(type, configuration, fileId, chunkNo, replicationDegree, 0, null);
        byte[] message = completeMessage(configuration, body, header);
        return message;
    }

    //backup enhancement
    protected byte[] buildMessage(String type, int configuration, String fileId, int chunkNo, int replicationDegree, byte[] body, float completionPercentage) {
        byte[] header = buildHeader(type, configuration, fileId, chunkNo, replicationDegree, completionPercentage, null);
        byte[] message = completeMessage(configuration, body, header);
        return message;
    }

    //restore enhancement
    protected byte[] buildMessage(String type, int configuration, String fileId, int chunkNo, int replicationDegree, String connection) {
        byte[] header = buildHeader(type, configuration, fileId, chunkNo, replicationDegree, 0, connection);
        return header;
    }

    private byte[] completeMessage(int configuration, byte[] body, byte[] header) {
        int config = configuration >>> 5;
        byte[] message = null;
        if (config % 2 == 1) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(header);
                outputStream.write(body);
                message = outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            message = header;
        }
        return message;
    }

    public int getSleep_time_ms() {
        return sleep_time_ms;
    }

    public static int buildSleep_time_ms() {
        return (int) (Math.random() * (MAX_TIME_SLEEP_MS + 1));
    }

    public abstract void handle();
}
