package app;

import protocol.subprotocol.RMIInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestApp {

    public final static String BACKUP = "BACKUP";
    public final static String RESTORE = "RESTORE";
    public final static String DELETE = "DELETE";
    public final static String RECLAIM = "RECLAIM";
    public final static String STATE = "STATE";

    private static String sub_protocol;
    private static String opnd_1 = "";
    private static String opnd_2 = "";

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: app.TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2> ");
            return;
        } else if (args.length == 4) {
            opnd_2 = args[3];
        }

        if (args.length >= 3) {
            opnd_1 = args[2];
        }

        String[] ap = args[0].split(":");
        String host, peer_ap;
        if (ap.length == 1) {
            host = "localhost";
            peer_ap = ap[0];
        } else {
            host = ap[0];
            peer_ap = ap[1];
        }

        sub_protocol = args[1];

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            RMIInterface initiator = (RMIInterface) registry.lookup(peer_ap);

            String message = sub_protocol + " " + opnd_1 + " " + opnd_2;
            String[] cmd = message.split(" ");

            //Parameters check
            if (sub_protocol.equals(BACKUP)) {
                if (args.length != 4) {
                    System.out.println("Usage: app.TestApp <peer_ap> " + sub_protocol + "  <filepath> <replication_degree> ");
                    return;
                }
                initiator.backup(cmd);
            } else if (sub_protocol.equals(RESTORE)) {
                if (args.length != 3) {
                    System.out.println("Usage: app.TestApp <peer_ap> " + sub_protocol + "  <filepath>");
                    return;
                }
                initiator.restore(cmd);
            } else if (sub_protocol.equals(DELETE)) {
                if (args.length != 3) {
                    System.out.println("Usage: app.TestApp <peer_ap> " + sub_protocol + "  <filepath>");
                    return;
                }
                initiator.delete(cmd);
            } else if (sub_protocol.equals(RECLAIM)) {
                if (args.length != 3) {
                    System.out.println("Usage: app.TestApp <peer_ap> " + sub_protocol + "  <max_size>");
                    return;
                }
                initiator.reclaim(cmd);
            } else if (sub_protocol.equals(STATE)) {
                if (args.length != 2) {
                    System.out.println("Usage: app.TestApp <peer_ap> " + sub_protocol);
                    return;
                }
                initiator.state(cmd);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
