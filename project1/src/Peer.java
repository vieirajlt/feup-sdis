public class Peer {

    private static Float version;
    private static Integer id;

    private static MulticastChannel control;
    private static MulticastChannel backup;
    private static MulticastChannel restore;

    private static Channel cmd;

    public static void main(String[] args) {

        if (args.length != 6) {
            System.out.println("Usage: Peer <protocol_version> <server_id> <service_ap> <MC_ip:MC_port> <MDB_ip:MDB_port> <MDR_ip:MDR_port>");
            return;
        }

        version = Float.parseFloat(args[0]);
        id = Integer.parseInt(args[1]);

        String[] ap = args[2].split("/");
        if(ap.length == 1) {
            cmd = new Channel("localhost", ap[0]);
        } else {
            cmd = new Channel(ap[0], ap[1]);
        }

        String[] MC = args[3].split(":");
        control = new MulticastChannel(MC[0], MC[1]);

        String[] MDB = args[4].split(":");
        backup = new MulticastChannel(MDB[0], MDB[1]);

        String[] MDR = args[5].split(":");
        restore = new MulticastChannel(MDR[0], MDR[1]);

        //Threads Start

        /*Thread threadMC = new Thread(control);
        threadMC.start();

        Thread threadMDB = new Thread(backup);
        threadMDB.start();

        Thread threadMDR = new Thread(restore);
        threadMDR.start();*/

        Thread threadCMD = new Thread(cmd);
        threadCMD.start();
    }
}
