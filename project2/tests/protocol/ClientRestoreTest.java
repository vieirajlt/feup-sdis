package protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import protocol.Chunk;
import protocol.subprotocol.communication.tcp.Server;
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

        String filePath = args[2];



        SplitFile sF = new SplitFile(filePath);


        System.out.println("completed ");

        String msg = "RESTORE " + sF.getFileId();
        test.write(msg);
        System.out.println("mesg: " + msg);

        // executor.shutdown();
    }
}
