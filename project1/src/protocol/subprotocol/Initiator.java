package protocol.subprotocol;

import app.TestApp;
import protocol.Chunk;
import protocol.Peer;
import protocol.RMIInterface;
import protocol.subprotocol.FileManagement.SplitFile;
import protocol.subprotocol.handler.*;

import java.nio.charset.StandardCharsets;
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

    public boolean run(byte[] message) {
        String strMessage = new String(message, StandardCharsets.UTF_8);
        String[] cmd = strMessage.split(" ");

        if (cmd[0].equals(TestApp.BACKUP)) {
            backup(cmd);
        } else if (cmd[0].equals(TestApp.RESTORE)) {
            restore(cmd);
        } else if (cmd[0].equals(TestApp.DELETE)) {
            delete(cmd);
        } else if (cmd[0].equals(TestApp.RECLAIM)) {
            reclaim(cmd);
        } else if (cmd[0].equals(TestApp.STATE)) {
            state(cmd);
        } else {
            return false;
        }
        return true;
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
            new Thread(putchunkHandler).start();
        }
    }

    public synchronized void restore(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.getchunk");
        String filepath = cmd[1];

        SplitFile sf = new SplitFile(filepath);

        GetchunkHandler getchunkHandler = new GetchunkHandler(sf);
        new Thread(getchunkHandler).start();
    }

    public synchronized void delete(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.delete");
        String filepath = cmd[1];

        SplitFile sf = new SplitFile(filepath);

        DeleteHandler deleteHandler = new DeleteHandler(sf);
        new Thread(deleteHandler).start();
    }


    public synchronized void reclaim(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.removed");

        long maxDiskSpace = Long.parseLong(cmd[1]);

        RemovedHandler removedHandler = new RemovedHandler(maxDiskSpace);
        new Thread(removedHandler).start();
    }

    public synchronized void state(String[] cmd) {
        System.out.println("protocol.subprotocol.Initiator.state");

        StateHandler stateHandler = new StateHandler();
        new Thread(stateHandler).start();
    }

}
