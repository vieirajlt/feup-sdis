import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.communication.tcp.Client;
import server.ClientSocket;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientSignupTest {

    private String peerID;
    private ScheduledThreadPoolExecutor executor;

    public static void main(String[] args) {
        ClientSignupTest t = new ClientSignupTest();
        t.run(args);
    }

    void run(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        peerID = args[2];

        ClientSocket test = new ClientSocket(host, port);

        test.write("SIGNUP " + peerID + " " + Math.round(100000 + Math.random() * 600000));

        executor =
                (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(7);

        while (true) { //TODO SLIPT THIS THING
            try {
                String msg = test.read();

                if (msg != null) {
                    executor.execute(() -> handleMsg(test, msg));
                } else {
                    System.out.println("NULL");
                }
            } catch (Exception e) {
                System.err.println("Error");
                e.printStackTrace();
            }
        }
    }

    private void handleMsg(ClientSocket test, String msg) {
        String host;
        String[] msgSplitted = msg.split(" ");
        String header = msgSplitted[0];

        if (header.equalsIgnoreCase("backup")) test.write("Accepted");
        else if (header.equalsIgnoreCase("receive")) {

            host = msgSplitted[1];
            String sPort = msgSplitted[2];
            int fileSize = Integer.parseInt(msgSplitted[3]);
            String fileID = msgSplitted[4];

            Client client = new Client(sPort, host);
            System.out.println("Received " + msg);
            List<Chunk> chunks = client.receiveChunk(peerID);

            for (Chunk chunk : chunks) {
                System.out.println("Received chunk " + chunk.getSize());
                executor.execute(() -> chunk.store(fileID));
            }

            test.write("STORED " + peerID + " " + fileID);

        }else if(header.equalsIgnoreCase("delete")){
            String fileId = msgSplitted[1];
            System.out.println("Will delete chunks of file " + fileId);
            File dir = new File("TMP/peer" + peerID + "/backup/" + fileId);
            String [] files = dir.list();


                for(String file: files){
                    File deleteFile = new File(dir.getPath(), file);
                    deleteFile.delete();
                }

            //delete all the file chunks from peersChunks
            Peer.getDataContainer().deletePeersFileChunks(fileId);
            Peer.getDataContainer().deleteBackedUpFileChunks(fileId);
        }
        else System.out.println(msg);
    }
}
