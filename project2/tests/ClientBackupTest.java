import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import protocol.Chunk;
import protocol.subprotocol.communication.tcp.Server;
import server.ClientSocket;

public class ClientBackupTest {

  public static void main(String[] args) {
    String host = args[0];
    int port = Integer.parseInt(args[1]);

    ClientSocket test = new ClientSocket(host, port);

    ScheduledThreadPoolExecutor executor =
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);

    int replicationDegree = 3;
    List<String> sockets = new ArrayList<>();

    for (int i = 0; i < replicationDegree; i++) {
      String msg = "Hey, this is a message";
      Chunk chunk = new Chunk(0, msg.getBytes());

      Server server = new Server(chunk);
      sockets.add(server.getConnectionSettings());
      executor.schedule(server::sendChunk, 2, TimeUnit.SECONDS);
    }

    StringBuilder sB = new StringBuilder();
    sB.append("BACKUP 1as98d21hiwdhwadh19832rhwqi " + replicationDegree + " 45000 #");
    for (String socket : sockets) {
      sB.append(socket);
      sB.append(" ");
    }

    String msg = sB.toString();
    test.write(msg);

    msg = test.read();

    System.out.println(msg);
  }
}
