package protocol;

import protocol.subprotocol.Initiator;
import protocol.subprotocol.Receiver;

public class Peer {

    private static Float version;
    private static int id;
    private static String ap;

    private static MulticastChannel control;
    private static MulticastChannel backup;
    private static MulticastChannel restore;

    private static Receiver protocolRec;

    private static DataContainer dataContainer;

    public static void main(String[] args) {

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
        protocolRec = new Receiver();

        dataContainer = DataContainer.load();

        Thread hook = new Thread(() -> {
            dataContainer.store();
        });
        Runtime.getRuntime().addShutdownHook(hook);

        //Threads Start

        Thread threadMC = new Thread(control);
        threadMC.start();

        Thread threadMDB = new Thread(backup);
        threadMDB.start();

        Thread threadMDR = new Thread(restore);
        threadMDR.start();

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
        if (!protocolRec.run(message)) {
            System.out.println("Something went wrong...");
        }
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
}
