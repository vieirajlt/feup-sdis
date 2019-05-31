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
import protocol.subprotocol.fileManagement.RestoreFile;
import protocol.subprotocol.fileManagement.SplitFile;
import server.ClientSocket;

public class ClientRestoreTest {

    public static void main(String[] args) throws IOException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ClientSocket test = new ClientSocket(host, port);

        ScheduledThreadPoolExecutor executor =
                (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);


        List<String> sockets = new ArrayList<>();


        String clientId = args[2];

        String filePath = args[3];

        SplitFile sF = new SplitFile(filePath);


        System.out.println("completed ");

        String msg = "RESTORE " + sF.getFileId();
        test.write(msg);
        System.out.println("mesg: " + msg);


        int counter = 0;
        while (true) {
            String rMSG = test.read();
            executor.schedule(() ->{
                System.out.println("RECEIVED " + rMSG);
                String connection = rMSG.split("#")[1];
                String rAddr = connection.split(":")[0];
                String rPort = connection.split(":")[1];
                Client client = new Client(rPort, rAddr);
                List<Chunk> chunks = client.receiveChunk("1");
                RestoreFile rF = new RestoreFile(chunks);
                String[] pathTokens = filePath.split("/");
                rF.process(clientId, pathTokens[pathTokens.length - 1]);
                executor.shutdownNow();


            }, counter++,  TimeUnit.SECONDS );
        }

        // executor.shutdown();
    }
}
