package protocol.subprotocol;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.FileManagement.SplitFile;
import protocol.subprotocol.handler.*;

import java.rmi.AlreadyBoundException;
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
            registry.bind(Peer.getAp(), stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void backup(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.putchunk");
        String filepath = cmd[1];
        int repDegree = Integer.parseInt(cmd[2]);

        SplitFile sf = new SplitFile(filepath, repDegree);

        Peer.getDataContainer().addOwnFile(sf.getFileId(), sf.getFile().getName(), sf.getChunks().size(), sf.getReplicationDegree());

        for (Chunk chunk : sf.getChunks()) {
            String chunkId = sf.getFileId() + "_" + chunk.getChunkNo();
            Peer.getDataContainer().addStored(chunkId);
            PutchunkHandler putchunkHandler = new PutchunkHandler(chunk, sf);
            Peer.getExecutor().execute(putchunkHandler);
        }
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
