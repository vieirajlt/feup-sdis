package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import protocol.Peer;

public class CentralServer implements Serializable {
  private transient ScheduledThreadPoolExecutor executor;

  /** KeyStore for storing our public/private key pair */
  private KeyStore clientKeyStore;

  /** KeyStore for storing the server's public key */
  private KeyStore serverKeyStore;

  /** Used to generate a SocketFactory */
  private SSLContext sslContext;

  private HashMap<String, List<String>> chunkLog;

  private static String passphrase = "serverpw";

  /** A source of secure random numbers */
  private static SecureRandom secureRandom = new SecureRandom();

  int port = 8585;

  private CentralServer() {
    this.chunkLog = new HashMap<>();

    int pool_size = Peer.getMaxThreadPoolSize();
    if (pool_size < 5) pool_size = 5;

    executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(pool_size);

    try {
      setupClientKeyStore();
      setupServerKeystore();
      setupSSLContext();

      SSLServerSocketFactory sf = sslContext.getServerSocketFactory();
      SSLServerSocket ss = (SSLServerSocket) sf.createServerSocket(port);

      // Require client authorization
      ss.setNeedClientAuth(true);

      System.out.println("Listening on port " + port + "...");
      while (true) {
        Socket socket = ss.accept();
        System.out.println("Got connection from " + socket);

        IncomingConnection connection = new IncomingConnection(socket, this);
        executor.execute(connection);
      }
    } catch (GeneralSecurityException | IOException gse) {
      gse.printStackTrace();
    }
  }

  private void read() {}

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

  public static void main(String[] args) {
    System.out.println("Will Start");
    CentralServer server = new CentralServer();

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

  private void setupClientKeyStore() throws GeneralSecurityException, IOException {
    clientKeyStore = KeyStore.getInstance("JKS");
    clientKeyStore.load(new FileInputStream("./certs/client.public"), "public".toCharArray());
  }

  private void setupServerKeystore() throws GeneralSecurityException, IOException {
    serverKeyStore = KeyStore.getInstance("JKS");
    serverKeyStore.load(new FileInputStream("./certs/server.private"), passphrase.toCharArray());
  }

  private void setupSSLContext() throws GeneralSecurityException, IOException {
    secureRandom.nextInt();

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(clientKeyStore);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(serverKeyStore, passphrase.toCharArray());

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
  }
}
