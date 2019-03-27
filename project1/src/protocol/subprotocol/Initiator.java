package protocol.subprotocol;

import app.TestApp;
import protocol.subprotocol.FileManagement.SplitFile;
import protocol.subprotocol.handler.DeleteHandler;
import protocol.subprotocol.handler.PutchunkHandler;
import protocol.subprotocol.handler.GetchunkHandler;

import java.nio.charset.StandardCharsets;

public class Initiator extends Subprotocol {

    public Initiator() {

    }


    public boolean run(byte[] message) {
        String strMessage = new String(message, StandardCharsets.UTF_8);
        String[] cmd = strMessage.split(" ");

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

    private void backup(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.putchunk");
            String filepath = cmd[1];
            int repDegree = Integer.parseInt(cmd[2]);

            SplitFile sf = new SplitFile(filepath, repDegree);

            PutchunkHandler putchunkHandler = new PutchunkHandler(sf);
            new Thread(putchunkHandler).start();
        }
    }

    private void restore(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.getchunk");
            String filepath = cmd[1];

            SplitFile sf = new SplitFile(filepath);

            GetchunkHandler getchunkHandler = new GetchunkHandler(sf);
            new Thread(getchunkHandler).start();
        }
    }

    private void delete(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.delete");
            String filepath = cmd[1];

            SplitFile sf = new SplitFile(filepath);

            DeleteHandler deleteHandler = new DeleteHandler(sf);
            new Thread(deleteHandler).start();
        }
    }

    private void reclaim(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.removed");
        }
    }

    private synchronized void state(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.state");
        }
    }

}
