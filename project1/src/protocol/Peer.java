package protocol;

import protocol.subprotocol.Initiator;
import protocol.subprotocol.Receiver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Peer {

    private static final int MAX_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private static Float version;
    private static int id;
    private static String ap;

    private static MulticastChannel control;
    private static MulticastChannel backup;
    private static MulticastChannel restore;

    private static DataContainer dataContainer;

    private static ScheduledThreadPoolExecutor executor;

    public static void main(String[] args) {

        System.out.println(MAX_THREAD_POOL_SIZE);

        if (args.length != 5) {
            System.out.println("Usage: protocol.Peer <protocol_version> <server_id> <MC_ip:MC_port> <MDB_ip:MDB_port> <MDR_ip:MDR_port>");
            return;
        }

        version = Float.parseFloat(args[0]);
        id = Integer.parseInt(args[1]);
        ap = "peer" + id;

        //address port
        String[] MC = args[2].split(":");
        control = new MulticastChannel(MC[0], MC[1]);

        String[] MDB = args[3].split(":");
        backup = new MulticastChannel(MDB[0], MDB[1]);

        String[] MDR = args[4].split(":");
        restore = new MulticastChannel(MDR[0], MDR[1]);

        Initiator.startInitiator();

        dataContainer = DataContainer.load();

        Thread hook = new Thread(() -> {
            dataContainer.store();
        });
        Runtime.getRuntime().addShutdownHook(hook);

        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(MAX_THREAD_POOL_SIZE);

        executor.execute(control);
        executor.execute(backup);
        executor.execute(restore);
    }

    public static MulticastChannel getControlChannel() {
        return control;
    }

    public static MulticastChannel getBackupChannel() {
        return backup;
    }

    public static MulticastChannel getRestoreChannel() {
        return restore;
    }

    public static void answerProtocol(byte[] message) {
        Receiver receiver = new Receiver(message);
        executor.execute(receiver);
    }

    public static Float getProtocolVersion() {
        return version;
    }

    public static int getServerId() {
        return id;
    }

    public static DataContainer getDataContainer() {
        return dataContainer;
    }

    public static String getAp() {
        return ap;
    }

    public static ScheduledThreadPoolExecutor getExecutor() {
        return executor;
    }
}
