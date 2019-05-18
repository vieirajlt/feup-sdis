package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.net.ssl.SSLServerSocket;
import protocol.Peer;

public class CentralServer extends SSLInit implements Serializable {
  private transient ScheduledThreadPoolExecutor executor;

  private ConcurrentHashMap<String, List<String>> chunkLog;

  private int port;

  private CentralServer(int port) {
    super("serverpw");

    this.port = port;
    this.chunkLog = new ConcurrentHashMap<>();

    int pool_size = Peer.getMaxThreadPoolSize();
    if (pool_size < 5) pool_size = 5;

    executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(pool_size);

    try {
      SSLServerSocket ss = initServer(port);

      // Require client authorization
      ss.setNeedClientAuth(true);

      System.out.println("Listening on port " + port + "...");
      while (true) {
        Socket socket = ss.accept();
        System.out.println("Got connection from " + socket);

        IncomingConnection connection = new IncomingConnection(socket, this);
        executor.execute(connection);
      }
    } catch (IOException gse) {
      gse.printStackTrace();
    }
  }

  public static void main(String[] args) {
    System.out.println("Will Start");

    int port = Integer.parseInt(args[0]);

    CentralServer server = new CentralServer(port);

    new Timer()
        .scheduleAtFixedRate(
            new TimerTask() {
              @Override
              public void run() {
                server.store();
              }
            },
            Peer.getSaveDataInterval(),
            Peer.getSaveDataInterval());

    Thread hook = new Thread(server::store);
    Runtime.getRuntime().addShutdownHook(hook);

    System.out.println("Ready to listen");
  }

  private void store() {
    Path path = Paths.get("./FILES/centralServer/serverLog");
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(path.toString());
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

      objectOutputStream.writeObject(this);
      objectOutputStream.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void request(String message) {
  }
}
