package protocol.subprotocol;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.fileManagement.SplitFile;
import protocol.subprotocol.handler.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Comparator;

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
        boolean enhanced = isEnhancementAllowed(BACKUP_SUBPROTOCOL);
        String filepath = cmd[1];
        int repDegree = Integer.parseInt(cmd[2]);

        SplitFile sf = new SplitFile(filepath, repDegree);

        sf.splitAndSend(enhanced);
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


    public static void initiateFileStatus() {
        boolean enhanced = isEnhancementAllowed(DELETE_SUBPROTOCOL);
        if(!enhanced)
           return;

        Path dirPath = Paths.get(Chunk.getPathname());
        if (!Files.exists(dirPath))
            return;
        try {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File->{
                        if(File.isDirectory() && !File.getName().equals("backup")) {
                            if( Peer.getDataContainer().getBackedUpChunkFileOwnerId(File.getName()) > -1)
                            {
                                Peer.getDataContainer().addTmpBackedUpFile(File.getName() , Peer.getDataContainer().getBackedUpChunkFileOwnerId(File.getName()));
                                FileStatusHandler handler = new FileStatusHandler(File.getName(), Peer.getDataContainer().getBackedUpChunkFileOwnerId(File.getName()));
                                handler.handle();
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
