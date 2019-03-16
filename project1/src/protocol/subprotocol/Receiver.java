package protocol.subprotocol;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.handler.Stored;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static protocol.subprotocol.handler.Handler.CR;
import static protocol.subprotocol.handler.Handler.LF;

public class Receiver extends Subprotocol {

    private final static String LOG_PATH_1 = "TMP/LOGS/";
    private final static String LOG_PATH_2 = "/replication.log";

    private static String pathname;

    public Receiver() {
        pathname = LOG_PATH_1 + Peer.getServerId() + LOG_PATH_2;
        File file = new File(pathname);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean run(String message) {
        String[] cmd = message.split(" ", 2);
        String regex = "" + CR + LF + CR + LF;
        String[] hb = message.split(regex, 2);

        if (cmd[0].equals(PUTCHUNK)) {
            backup(hb);
        } else if (cmd[0].equals(GETCHUNK)) {
            restore(hb);
        } else if (cmd[0].equals(REMOVED)) {
            reclaim(hb);
        } else if (cmd[0].equals(DELETE)) {
            delete(hb);
        } else if (cmd[0].equals(STORED)) {
            stored(hb);
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected void backup(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.backup");
            String[] header = cmd[0].split(" ");
            byte[] body = cmd[1].getBytes(StandardCharsets.UTF_8);

            if (!checkHeader(header))
                return;

            int chunkNo = Integer.parseInt(header[4]);
            Chunk chunk = new Chunk(chunkNo, body);

            String fileId = header[3];

            //case already backed up
            if(chunk.load(fileId, chunkNo) != null)
                return;

            chunk.store(fileId);

            Stored stored = new Stored(fileId, chunkNo);
            new Thread(stored).start();
        }
    }

    @Override
    protected void restore(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.restore");
        }
    }

    @Override
    protected void delete(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.delete");
        }
    }

    @Override
    protected void reclaim(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.reclaim");
        }
    }

    private boolean checkHeader(String[] header) {
        return Float.parseFloat(header[1]) == Peer.getProtocolVersion() &&
                Integer.parseInt(header[2]) != Peer.getServerId();
    }

    private void stored(String[] cmd) {
        System.out.println("protocol.subprotocol.Receiver.stored");
        String[] header = cmd[0].split(" ");
        String senderId = header[2];
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        //for visual verification
        String log_message = fileId + "," + chunkNo + "," + senderId + "\n";
        try {
            Files.write(
                    Paths.get(pathname),
                    log_message.getBytes(),
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String chunkId = fileId + "_" + chunkNo;
        Peer.getDataContainer().incCurrReoDegree(chunkId);

    }

}
