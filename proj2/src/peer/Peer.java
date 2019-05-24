package peer;

import connection.client.handler.Handler;
import connection.client.Client;
import peer.data.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Peer {

    private static final int MAX_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;

    private static final String TMP_PATH = "TMP/";

    private static Peer instance = null;

    private int id;
    private static String ap;
    private int central_port;
    private InetAddress central_address;

    private Client client;

    private Data data;

    private ScheduledThreadPoolExecutor executor;

    public static void main(String[] args) {

        System.setProperty("java.security.policy","file:FILES/policy");
        System.setProperty("javax.net.ssl.trustStore","FILES/samplecacerts");
        System.setProperty("javax.net.ssl.trustStorePassword","changeit");
        System.setProperty("javax.net.ssl.keyStore", "FILES/testkeys");
        System.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");

        if (args.length != 2) {
            System.out.println(
                    "Usage: peer.Peer <id> <host:port>");
            return;
        }

        int id = Integer.parseInt(args[0]);
        String[] addr = args[1].split(":");
        String host;
        int port;
        if(addr.length == 1) {
            host = "localhost";
            port = Integer.parseInt(addr[0]);
        } else {
            host = addr[0];
            port = Integer.parseInt(addr[1]);
        }

        port += id;

        try {
            instance = new Peer(id, host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String init_cmd = "INIT " + ap + " " + instance.getData().getStorageCapacity();

        Handler handler = new Handler();
        handler.handle(init_cmd);

        System.out.println("Ready with id " + id );

        //TODO for testing
        if(id == 1) {
            String init_bu = "BACKUP " + ap + " testfilename.file " + "500";
            handler.handle(init_bu);
        }

    }

    public Peer(int id, String host, int port) throws IOException {
        this.id = id;
        this.ap = "peer" + id;
        this.central_address = InetAddress.getByName(host);
        this.central_port = port;

        this.client = new Client(this.central_address, this.central_port);
        this.data = Data.load(id);

        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(MAX_THREAD_POOL_SIZE);

        executor.schedule(client, 0, TimeUnit.MILLISECONDS);

        Thread hook =
                new Thread(
                        () -> {
                            data.store();
                            //Path path = Paths.get(TMP_PATH);
                            //FileManager.clearEmptyFolders(path);
                        });
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public static Peer getInstance() {
        return instance;
    }

    public Client getClient() {
        return client;
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return executor;
    }

    public Data getData() {
        return data;
    }
}
