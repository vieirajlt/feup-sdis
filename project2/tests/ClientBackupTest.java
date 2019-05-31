import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import protocol.Chunk;
import protocol.subprotocol.communication.tcp.Client;
import protocol.subprotocol.communication.tcp.Server;
import protocol.subprotocol.fileManagement.SplitFile;
import server.ClientSocket;

public class ClientBackupTest {

    public ClientBackupTest() {
    }

    public static void main(String[] args) throws IOException {
        ClientBackupTest cBT = new ClientBackupTest();
        cBT.run(args);
    }

    public void run(String[] args) throws IOException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ClientSocket test = new ClientSocket(host, port);

        ScheduledThreadPoolExecutor executor =
                (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);


        List<String> sockets = new ArrayList<>();

        String clientID = args[2];

        String filePath = args[3];

        int replicationDegree = Integer.parseInt(args[4]);

        File file = new File(filePath);
        int length = (int) (file.length());
        FileInputStream stream = new FileInputStream(file);
        byte[] bytes = new byte[length];
        int read = stream.read(bytes, 0, length);
        System.out.println("READ " + read + " bytes");
        System.out.println("FileSize " + length + " bytes");
        String msgToSend = "Hey, this is a message. Bigger than before";
        // length = msgToSend.length();
        // Chunk chunk = new Chunk(0, msgToSend.getBytes());
        Chunk chunk = new Chunk(0, bytes);

        SplitFile sF = new SplitFile(filePath, replicationDegree);
        sF.splitAndSend(false);

        Runnable runnable = new Runnable() {
            public void run() {
                if (!sF.completed) {
                    executor.schedule(this, 100, TimeUnit.MILLISECONDS);
                    return;
                }

                System.out.println("completed ");

                for (int i = 0; i < replicationDegree; i++) {

                    System.out.println("READ " + sF.getChunks().size() + " CHUNKS!!");

                    Server server = new Server(sF.getChunks());
                    sockets.add(server.getConnectionSettings());
                    executor.schedule(server::sendChunk, 2, TimeUnit.SECONDS);
                }

                StringBuilder sB = new StringBuilder();
                sB.append("BACKUP " + sF.getFileId() + " " + replicationDegree + " " + length + " #");
                for (String socket : sockets) {
                    sB.append(socket);
                    sB.append(" ");
                }

                String msg = sB.toString();
                test.write(msg);
                System.out.println("WROTE " + msg);

                String rMSG = test.read();

                System.out.println("RECEIVED " + rMSG);
            }
        };

        executor.execute(runnable);
    }


}
