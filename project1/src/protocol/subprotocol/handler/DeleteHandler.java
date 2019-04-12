package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.subprotocol.fileManagement.SplitFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static protocol.subprotocol.Subprotocol.DELETE;

public class DeleteHandler extends Handler implements Runnable {

    private static final int MAX_DELETE_REPEAT = 5;

    private static final int DELETE_INBETWEEN_TIME_MS = 1000;

    private int repeatCnt = 0;

    private SplitFile sf;

    private byte[] message;

    public DeleteHandler(SplitFile sf) {
        this.sf = sf;
    }

    @Override
    public void handle() {

        message = buildMessage(DELETE, MSG_CONFIG_DELETE, sf.getFileId(), -1, -1, (byte[]) null);

        Peer.getExecutor().execute(this);

        //delete the file
        Path path = Paths.get(sf.getPathname());
        try {
            Files.deleteIfExists(path);
            System.out.println("File deleted successfully " + Files.exists(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to delete the file");
            return;
        }

        //delete file from Peer's ownFiles and file chunks from stored
        Peer.getDataContainer().deleteOwnFileAndChunks(sf.getFileId());
    }

    @Override
    public void run() {
        if (repeatCnt < MAX_DELETE_REPEAT) {
            Peer.getControlChannel().write(message);
            ++repeatCnt;
            Peer.getExecutor().schedule(this, DELETE_INBETWEEN_TIME_MS, TimeUnit.MILLISECONDS);
        }
    }
}
