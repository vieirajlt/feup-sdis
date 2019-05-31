package server;

import java.io.*;
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
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLServerSocket;

import protocol.Peer;
import protocol.info.ChunkInfo;
import server.info.ActivePeer;

public class CentralServer extends SSLInit implements Serializable {
    private transient ScheduledThreadPoolExecutor executor;

    private ConcurrentHashMap<String, List<String>> chunkLog; // key is File/Chunk ID (hash) value is peerID
    private transient ConcurrentHashMap<String, ActivePeer> peers; // key is Peer ID

    private CentralServer(int port) {
        super("serverpw");

        load();
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

    private void load() {
        Path path = Paths.get("./TMP/centralServer/serverLog");

        if (path.toFile().exists()) {
            System.out.println("Loading myself!");
            try {
                FileInputStream fileInputStream = new FileInputStream(path.toString());
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

                chunkLog = (ConcurrentHashMap<String, List<String>>) objectInputStream.readObject();
                objectInputStream.close();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else
            chunkLog = new ConcurrentHashMap<>();
    }

    void request(String message, IncomingConnection connection) {

        try {
            String[] parts = message.split(" ");
            System.out.println("Received request: " + Arrays.toString(parts));

            String header = parts[0];
            String fileID;
            switch (header.toLowerCase()) {
                case "backup":
                    fileID = parts[1];
                    int replicationDegree = Integer.parseInt(parts[2]);
                    int fileSize = Integer.parseInt(parts[3]);

                    String sockets = message.split("#")[1];
                    String[] adrs = sockets.split(" ");

                    backup(fileID, replicationDegree, fileSize, connection, adrs);
                    break;

                case "restore":

                    fileID = parts[1];

                    if (!isBackedUp(fileID)) {
                        System.err.println("Unrecognized fileID\t" + fileID);
                        break;
                    }

                    List<String> peersList = chunkLog.get(fileID);

                    for (String peerID : peersList) {
                        executor.execute(() -> restore(peerID, message, connection));
                    }


                    break;

                case "delete":
                    fileID = parts[1];
                    delete(fileID);
                    break;

                case "signup":
                    String peerID = parts[1];
                    long freeSpace = Long.parseLong(parts[2]);

                    signUp(peerID, freeSpace, connection);
                    break;
                case "signout":
                    peerID = parts[1];
                    signOut(peerID);
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
        System.out.println("Removed peer " + peerID);

    }


    private void restore(String peerID, String message, IncomingConnection connection) {

        ActivePeer peer = peers.get(peerID);
        try {
            peer.getOutput().writeUTF(message);
            String response = peer.getInput().readUTF();
            System.out.println("res: " + response);

            connection.getOutputStream().writeUTF(response);

        } catch (IOException e) {
            e.printStackTrace();
        }

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
                1000L,
                (ActivePeer peer) -> {
                    try {
                        String[] components = new String[]{"BACKUP", fileID, Integer.toString(fileSize)};
                        String msg = String.join(" ", components);

                        if (!peer.enoughSpace(fileSize) || peer.isBusy() || availablePeers.size() >= replicationDegree) {
                            return;
                        }

                        peer.getOutput().writeUTF(msg);
                        String response = peer.getInput().readUTF();
                        if (response.equalsIgnoreCase("accepted")) {
                            peer.setBusy(true);
                            availablePeers.add(peer);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        System.out.println(peers.toString());
        try {
            connection.getOutputStream().writeUTF("HEY ");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                                                new String[]{"RECEIVE", host, port, Integer.toString(fileSize), fileID};
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

        for (ActivePeer peer :
                availablePeers) {
            executor.execute(() -> {


                try {
                    String response = peer.getInput().readUTF();
                    System.out.println("Response " + response);


                    String[] messageTokens = response.split(" ");
                    String header = messageTokens[0];

                    if (header.equalsIgnoreCase("stored")) {


                        String peerID = messageTokens[1];
                        String file = messageTokens[2];

                        this.addChunkLog(file, peerID);
                        peer.setBusy(false);
                    } else {
                        System.err.println("Unrecognized header " + header);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.schedule(() -> System.out.println(this.chunkLog.toString()), 10, TimeUnit.SECONDS);
    }


    private void addChunkLog(String fileID, String peerID) {
        List<String> list = this.chunkLog.get(fileID);
        if (list == null) {
            List<String> peers = new LinkedList<>();
            peers.add(peerID);
            this.chunkLog.put(fileID, peers);
        } else {
            list.add(peerID);
        }
    }


    private int delete(String fileID) {
        System.out.println("Will delete " + fileID);
        List<String> filePeers = this.chunkLog.get(fileID);
        if (filePeers == null) {
            System.out.println("No peers have this file.");
            this.chunkLog.remove(fileID);
            return 1;
        }
        for (String filePeer : filePeers) {
            ActivePeer aPeer = this.peers.get(filePeer);

            try {
                String[] components =
                        new String[]{"DELETE", fileID};
                String msg = String.join(" ", components);
                aPeer.getOutput().writeUTF(msg);
                System.out.println(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.chunkLog.remove(fileID);

        return 0;
    }

    private boolean isBackedUp(String fileID) {
        if (this.chunkLog.get(fileID) == null)
            return false;
        return true;
    }

}
