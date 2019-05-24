package connection.server.handler;

import central.Central;
import connection.server.Server;

import java.io.IOException;
import java.util.ArrayList;

public class Handler {

    private final static String INIT = "INIT"; //INIT <peer_ap> <initial_size>
    public final static String BACKUP = "BACKUP"; //BACKUP <peer_ap> <file_name> <file_size>
    public final static String OPEN = "OPEN"; //OPEN <file_name> <host:port>

    public void handle(String message) {
        String[] separated = message.split(" ");
        if(separated[0].equals(INIT)) {
            init(separated);
        } else if(separated[0].equals(BACKUP)) {
            backup(separated);
        } else if(separated[0].equals(OPEN)) {
            connect(separated);
        } else {
            store(message);
        }
    }

    //0.2*
    private void init(String[] separated) {
        if(separated.length != 3)
            return;

        String ap = separated[1];
        Integer size = Integer.parseInt(separated[2]);

        Central.getInstance().getData().storeInitPeer(ap, size);

        String print = "Added " + ap + " with size " + size + " bytes.";

        System.out.println(print);

        Server server = Central.getInstance().getPeerServer(ap);
        try {
            server.write(print.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //1.2
    private void backup(String[] separated) {
        if(separated.length != 4)
            return;

        String ap = separated[1];
        String filename = separated[2];
        Integer size = Integer.parseInt(separated[3]);

        Central.getInstance().getData().addOwner(filename, ap);

        ArrayList<String> available = Central.getInstance().getData().findAvailablePeers(ap, size);

        System.out.println("Found " + available.size() + " available peers for storage.");

        Server server = Central.getInstance().getPeerServer(available.get(0));

        String query = BACKUP + " " + filename;

        try {
            server.write(query.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //1.4
    private void connect(String[] separated) {
        if(separated.length != 3)
            return;

        String filename = separated[1];
        String host = separated[2];
        String ap = Central.getInstance().getData().getOwner(filename);

        Server server = Central.getInstance().getPeerServer(ap);

        String query = OPEN + " " + filename + " " + host;

        try {
            server.write(query.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Returned new socket info to file owner");
    }

    //1.6
    private void store(String message) {
        System.out.println("Received as if was a file: " + message);
    }
}
