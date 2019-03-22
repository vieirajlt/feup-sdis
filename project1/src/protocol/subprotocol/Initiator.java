package protocol.subprotocol;

import app.TestApp;
import protocol.subprotocol.FileManagement.SplitFile;
import protocol.subprotocol.handler.PutchunkHandler;
import protocol.subprotocol.handler.GetchunkHandler;

public class Initiator extends Subprotocol {

    public Initiator() {

    }

    @Override
    public boolean run(String message) {
        String[] cmd = message.split(" ");

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

    @Override
    protected void backup(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.backup");
            String filepath = cmd[1];
            int repDegree = Integer.parseInt(cmd[2]);

            SplitFile sf = new SplitFile(filepath, repDegree);

            PutchunkHandler putchunkHandler = new PutchunkHandler(sf);
            new Thread(putchunkHandler).start();
        }
    }

    @Override
    protected void restore(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.restore");

            System.out.println(cmd[0]); // RESTORE
            System.out.println(cmd[1]); // FILEPATH

            String filepath = cmd[1];

            SplitFile sf = new SplitFile(filepath);
            GetchunkHandler getchunkHandler = new GetchunkHandler(sf);
            new Thread(getchunkHandler).start();
        }
    }

    @Override
    protected void delete(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.delete");
        }
    }

    @Override
    protected void reclaim(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.reclaim");
        }
    }

    private synchronized void state(String[] cmd) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Initiator.state");
        }
    }

}
