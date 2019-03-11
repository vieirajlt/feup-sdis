public class SubProtocol {

    private final static String PUTCHUNK = "PUTCHUNK";
    private final static String STORED = "STORED";

    private final static int SLEEP_TIME_MS = 200;

    private static SubProtocol instance = null;

    private SubProtocol() {
    }

    public static SubProtocol getInstance() {
        if(instance == null) {
            instance = new SubProtocol();
        }
        return instance;
    }

    public boolean run(String message) {
        String[] cmd = message.split(" ");

        if(cmd[0].equals(TestApp.BACKUP)) {
            backup(cmd);
        } else if(cmd[0].equals(TestApp.RESTORE)) {
            restore(cmd);
        } else if(cmd[0].equals(TestApp.DELETE)) {
            delete(cmd);
        } else if(cmd[0].equals(TestApp.RECLAIM)) {
            reclaim(cmd);
        } else if(cmd[0].equals(TestApp.STATE)) {
            state(cmd);
        } else {
            return false;
        }
        return true;
    }

    private synchronized void backup(String[] cmd) {
        System.out.println("SubProtocol.backup");

        String fileName = cmd[1];
        int replicationDegree = Integer.parseInt(cmd[2]);

        //divide file in chunks

        String message = buildMessage(PUTCHUNK);

        Peer.getBackupChannel().write(message);

        try {
            Thread.sleep(SLEEP_TIME_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //for each chunk, register stored
    }

    private synchronized void restore(String[] cmd) {
        System.out.println("SubProtocol.restore");
    }

    private synchronized void delete(String[] cmd) {
        System.out.println("SubProtocol.delete");
    }

    private synchronized void reclaim(String[] cmd) {
        System.out.println("SubProtocol.reclaim");
    }

    private synchronized void state(String[] cmd) {
        System.out.println("SubProtocol.state");
    }

    private String buildMessage(String cmd) {
        return cmd;
    }
}
