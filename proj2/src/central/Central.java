package central;

import central.data.Data;
import connection.server.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Central {

    private static final int MAX_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;

    private static final String TMP_PATH = "TMP/";

    private static Central instance = null;

    private int port;
    private InetAddress address;

    private static int slots;

    private ArrayList<Server> servers;

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
                    "Usage: central.Central <connection_port> <slots>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        slots = Integer.parseInt(args[1]);
        try {
            instance = new Central(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Ready on " + instance.getAddress() + ":" + instance.getPort());

    }

    public Central(int port) throws IOException {
        this.port = port;
        this.address = InetAddress.getLocalHost();
        this.servers = new ArrayList<>();

        this.data = Data.load();

        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(MAX_THREAD_POOL_SIZE);

        for(int i = 1; i <= slots; ++i) {
            Server server = Server.create(port + i);
            this.servers.add(server);
            executor.schedule(server, 0, TimeUnit.MILLISECONDS);
        }

        Thread hook =
                new Thread(
                        () -> {
                            data.store();
                            //Path path = Paths.get(TMP_PATH);
                            //FileManager.clearEmptyFolders(path);
                        });
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public static Central getInstance() {
        return instance;
    }

    public Data getData() {
        return data;
    }

    public Server getPeerServer(String ap) {
        int index = Integer.parseInt(ap.substring(4));
        index -= 1;
        return servers.get(index);
    }
}
