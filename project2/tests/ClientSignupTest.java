import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.communication.tcp.Client;
import protocol.subprotocol.communication.tcp.Server;
import server.ClientSocket;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class ClientSignupTest {

    private String peerID;
    private ScheduledThreadPoolExecutor executor;

    private ConcurrentHashMap<String, Integer> chunkLog; // key is File/Chunk ID (hash) value is nr of chunks stored

    public ClientSignupTest() {
        chunkLog = new  ConcurrentHashMap<>();
    }

    public static void main(String[] args) {
        ClientSignupTest t = new ClientSignupTest();
        t.run(args);
    }

    void run(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        peerID = args[2];

        ClientSocket test = new ClientSocket(host, port);

        test.write("SIGNUP " + peerID + " " + Math.round(10000000 + Math.random() * 600000));

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

        switch (header.toLowerCase()) {
            case "backup" :
                test.write("Accepted");
                break;
            case "receive" :

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
                chunkLog.put(fileID, chunks.size());

                System.out.println("peer chunklog " + chunkLog.toString());

                break;
            case "restore" :

                System.out.println("here: " + msg);

                fileID = msgSplitted[1];

                int nrChunks = chunkLog.get(fileID);

                List<Chunk> listChunks = new LinkedList<>();

                for(int i = 0; i < nrChunks; i++) {
                    Chunk chunk = new Chunk(i, peerID);
                    chunk.load(fileID);
                    listChunks.add(chunk);
                }

                completeLoad(listChunks);

                Server server = new Server(listChunks);
               // sockets.add(server.getConnectionSettings());
                executor.execute(server::sendChunk);

                StringBuilder sB = new StringBuilder();
                sB.append("RESTOREPORT " + fileID + " #" + server.getConnectionSettings());

                test.write(sB.toString());

                break;
                default:
                    System.out.println(msg);
                    break;


       /* if (header.equalsIgnoreCase("backup")) test.write("Accepted");
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
            chunkLog.add(fileID);

            System.out.println("peer chunklog " + chunkLog.toString());

        } else System.out.println(msg);*/
            case "delete":
            String fileId = msgSplitted[1];
            System.out.println("Will delete chunks of file " + fileId);
            File dir = new File("TMP/peer" + peerID + "/backup/" + fileId);
            String [] files = dir.list();


            for(String file: files){
                File deleteFile = new File(dir.getPath(), file);
                deleteFile.delete();
            }
            if(dir.isDirectory() && dir.list().length == 0)
                dir.delete();

            System.out.println("Finished deleting chunks");
            break;
        }

    }


    private void completeLoad(List<Chunk> chunks) {
        boolean completed = false;

        while(!completed) {
            boolean allLoaded = true;
            for(Chunk chunk : chunks) {
                if(!chunk.isLoaded())
                    allLoaded = false;
            }
            if(allLoaded)
                completed = true;

        }
    }
}
