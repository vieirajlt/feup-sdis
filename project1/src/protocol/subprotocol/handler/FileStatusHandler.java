package protocol.subprotocol.handler;

import protocol.Peer;

import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.FILESTATUS;

public class FileStatusHandler extends Handler implements Runnable {

    private static final int MAX_FILESTATUS_REPEAT = 5;

    private static final int FILESTATUS_INBETWEEN_TIME_MS = 1000;

    private int repeatCnt = 0;


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
        if (repeatCnt < MAX_FILESTATUS_REPEAT) {
            if(Peer.getDataContainer().getTmpBackedUpFileResponse(fileId , fileOwner) != -1)
                return;
            Peer.getControlChannel().write(message);
            ++repeatCnt;
            Peer.getExecutor().schedule(this,FILESTATUS_INBETWEEN_TIME_MS, TimeUnit.MILLISECONDS);
        } else {
            if(Peer.getDataContainer().getTmpBackedUpFileResponse(fileId , fileOwner) != -1)
                return;
            repeatCnt = 0;
            Peer.getExecutor().schedule(this, 2, TimeUnit.HOURS);
        }
    }
}
