package protocol.subprotocol;

import protocol.Peer;
import protocol.subprotocol.FileManagement.SplitFile;
import protocol.subprotocol.handler.DeleteHandler;
import protocol.subprotocol.handler.GetchunkHandler;
import protocol.subprotocol.handler.RemovedHandler;
import protocol.subprotocol.handler.StateHandler;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Initiator extends Subprotocol implements RMIInterface {

    public static void startInitiator() {
        try {
            Initiator obj = new Initiator();
            RMIInterface stub = (RMIInterface) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(Peer.getAp(), stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public synchronized void backup(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.putchunk");
        String filepath = cmd[1];
        int repDegree = Integer.parseInt(cmd[2]);

        SplitFile sf = new SplitFile(filepath, repDegree);

        sf.splitAndSend();
    }

    public synchronized void restore(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.getchunk");
        String filepath = cmd[1];

        SplitFile sf = new SplitFile(filepath);

        GetchunkHandler getchunkHandler = new GetchunkHandler(sf);
        getchunkHandler.handle();
    }

    public synchronized void delete(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.delete");
        String filepath = cmd[1];

        SplitFile sf = new SplitFile(filepath);

        DeleteHandler deleteHandler = new DeleteHandler(sf);
        deleteHandler.handle();
    }


    public synchronized void reclaim(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.removed");

        long maxDiskSpace = Long.parseLong(cmd[1]);

        RemovedHandler removedHandler = new RemovedHandler(maxDiskSpace);
        removedHandler.handle();
    }

    public synchronized void state(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.state");

        StateHandler stateHandler = new StateHandler();
        stateHandler.handle();
    }

}
