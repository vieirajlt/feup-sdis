import protocol.Chunk;
import protocol.subprotocol.communication.tcp.Client;
import server.ClientSocket;

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
                executor.schedule(() -> chunk.store(fileID), 1, TimeUnit.MILLISECONDS);
                System.out.println(executor.getQueue());
            }

        } else System.out.println(msg);
    }
}
