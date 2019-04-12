package protocol.subprotocol.handler;

import protocol.Peer;

import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.FILESTATUS;

public class FileStatusHandler extends Handler implements Runnable {

    private String fileId;
    private int fileOwner;
    private byte[] message;

    public FileStatusHandler(String fileId, int fileOwner) {
        this.fileId = fileId;
        this.fileOwner = fileOwner;
    }

    @Override
    public void handle() {
        message = buildMessage(FILESTATUS, MSG_CONFIG_FILESTATUS, fileId, fileOwner);
        Peer.getExecutor().schedule(this, getSleep_time_ms(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        Peer.getControlChannel().write(message);
    }
}
