package connection.client.handler;

import connection.client.Client;
import connection.server.Server;
import peer.Peer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class Handler {

    public final static String INIT = "INIT"; //INIT <peer_ap> <storage_capacity>
    public final static String BACKUP = "BACKUP"; //BACKUP <peer_ap> <file_name> <file_size>
    public final static String OPEN = "OPEN"; //OPEN <file_name> <host:port>

    public void handle(String message) {
        String[] separated = message.split(" ");
        if(separated[0].equals(INIT) && separated.length == 3) {
            init(message);
        } else if(separated[0].equals(BACKUP) && separated.length == 4) { //sending
            backup(message);
        } else if(separated[0].equals(BACKUP) && separated.length == 2) { //receiving
            openSocket(separated[1]);
        } else if(separated[0].equals(OPEN) && separated.length == 3) { //receiving
            open(separated);
        }
    }

    //0.1
    private void init(String query) {
        try {
            Peer.getInstance().getClient().write(query.getBytes());
            System.out.println("Request sent: " + query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //1.1
    private void backup(String query) {
        try {
            Peer.getInstance().getClient().write(query.getBytes());
            System.out.println("Request sent: " + query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //1.3
    private void openSocket(String filename) {
        try (Server server = Server.create()) {
            Peer.getInstance().getExecutor().schedule(server, 0, TimeUnit.MILLISECONDS);
            String query = OPEN + " " + filename + " " + server.getAddr() + ":" + server.getLocalPort();

            Peer.getInstance().getClient().write(query.getBytes());

            System.out.println("Opened Server Socket: " + query);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //1.5
    private void open(String[] separated) {
        if(separated.length != 3)
            return;

        String filename = separated[1];
        String[] host = separated[2].split(":");

        try {
            InetAddress addr = InetAddress.getByName(host[0]);
            int port = Integer.parseInt(host[1]);
            Client client = new Client(addr, port);
            client.write(filename.getBytes()); //TODO Just for testing, should send chunks here

            System.out.println("Wrote to Server Socket.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
