package protocol.subprotocol.handler;

import protocol.Peer;

import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.STATUS;

public class StatusHandler extends Handler implements Runnable {


    private String fileId;
    private int status;
    private byte[] message;

    public StatusHandler(String fileId, int status) {
        this.fileId = fileId;
        this.status = status;
    }

    @Override
    public void handle() {
        message = buildMessage(STATUS, MSG_CONFIG_STATUS, fileId, -1,  status);
        Peer.getExecutor().execute(this);
    }

    @Override
    public void run() {
        System.out.println("statushandler.run");
        Peer.getControlChannel().write(message);
    }
}
