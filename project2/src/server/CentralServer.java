package server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.net.ssl.SSLServerSocket;
import protocol.Peer;
import protocol.info.ChunkInfo;
import server.info.ActivePeer;

public class CentralServer extends SSLInit implements Serializable {
  private transient ScheduledThreadPoolExecutor executor;

  private ConcurrentHashMap<String, List<ChunkInfo>> chunkLog; // key is File/Chunk ID (hash)
  private ConcurrentHashMap<String, ActivePeer> peers; // key is Peer ID

  private CentralServer(int port) {
    super("serverpw");

    this.chunkLog = new ConcurrentHashMap<>();
    this.peers = new ConcurrentHashMap<>();

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

  void request(String message, IncomingConnection connection) {

    try {
      String[] parts = message.split(" ");
      System.out.println("Received request: " + Arrays.toString(parts));

      String header = parts[0];

      switch (header.toLowerCase()) {
        case "backup":
          String fileID = parts[1];
          int replicationDegree = Integer.parseInt(parts[2]);
          int fileSize = Integer.parseInt(parts[3]);

          String sockets = message.split("#")[1];
          String[] adrs = sockets.split(" ");

          backup(fileID, replicationDegree, fileSize, connection, adrs);
          break;

        case "restore":
          break;

        case "delete":
          break;

        case "signup":
          String peerID = parts[1];
          long freeSpace = Long.parseLong(parts[2]);

          signUp(peerID, freeSpace, connection);
          break;

        default:
          System.err.println("Unrecognized Header\t" + header);
      }
    } catch (Exception e) {
      System.err.println("Request malformed\n" + e.toString());
    }
  }

  private void signUp(String peerID, long freeSpace, IncomingConnection conection) {
    ActivePeer newPeer = new ActivePeer(freeSpace, conection);
    this.peers.put(peerID, newPeer);
    System.out.println("Peer " + peerID + " registered");
    System.out.println(this.peers.size() + " peers online");

    try {
      conection.getOutputStream().writeUTF("Peer " + peerID + " registered");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void signOut(String peerID) {
    this.peers.remove(peerID);
  }

  private void backup(
      String fileID,
      int replicationDegree,
      int fileSize,
      IncomingConnection connection,
      String[] adrs) {
    System.out.println("Will backup " + fileID);

    List<ActivePeer> availablePeers = new LinkedList<>();

    this.peers.forEachValue(
        1L,
        (ActivePeer peer) -> {
          try {
            String[] components = new String[] {"BACKUP", fileID, Integer.toString(fileSize)};
            String msg = String.join(" ", components);
            peer.getOutput().writeUTF(msg);
            String response = peer.getInput().readUTF();
            if (response.equalsIgnoreCase("accepted")) {
              availablePeers.add(peer);
            }

          } catch (IOException e) {
            e.printStackTrace();
          }
        });

    new java.util.Timer()
        .schedule(
            new java.util.TimerTask() {
              @Override
              public void run() {

                System.out.println("RUNNING");

                for (int i = 0; i < availablePeers.size(); i++) {
                  ActivePeer peer = availablePeers.get(i);

                  if (i >= adrs.length) {
                    return;
                  }

                  String adr = adrs[i];
                  String host = adr.split(":")[0];
                  String port = adr.split(":")[1];

                  try {
                    String[] components =
                        new String[] {"RECEIVE", host, port, Integer.toString(fileSize)};
                    String msg = String.join(" ", components);
                    peer.getOutput().writeUTF(msg);
                    System.out.println(msg);
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
              }
            },
            2000);
  }
}
