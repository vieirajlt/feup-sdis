import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class CentralServerTest {
  /** KeyStore for storing our public/private key pair */
  private KeyStore clientKeyStore;

  /** KeyStore for storing the server's public key */
  private KeyStore serverKeyStore;

  /** Used to generate a SocketFactory */
  private SSLContext sslContext;

  /** Passphrase for accessing our authentication keystore */
  private static final String passphrase = "clientpw";

  /** A source of secure random numbers */
  private static SecureRandom secureRandom = new SecureRandom();

  private void setupServerKeystore() throws GeneralSecurityException, IOException {
    serverKeyStore = KeyStore.getInstance("JKS");
    serverKeyStore.load(new FileInputStream("./certs/server.public"), "public".toCharArray());
  }

  private void setupClientKeyStore() throws GeneralSecurityException, IOException {
    clientKeyStore = KeyStore.getInstance("JKS");
    clientKeyStore.load(new FileInputStream("./certs/client.private"), passphrase.toCharArray());
  }

  private void setupSSLContext() throws GeneralSecurityException, IOException {
    secureRandom.nextInt();

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(serverKeyStore);

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(clientKeyStore, passphrase.toCharArray());

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
  }

  private void connect(String host, int port) {
    try {
      setupServerKeystore();
      setupClientKeyStore();
      setupSSLContext();

      SSLSocketFactory sf = sslContext.getSocketFactory();
      SSLSocket socket = (SSLSocket) sf.createSocket(host, port);

      InputStream in = socket.getInputStream();
      OutputStream out = socket.getOutputStream();

      DataInputStream din = new DataInputStream(in);
      DataOutputStream dout = new DataOutputStream(out);

      String msg = "BACKUP asdasd78a7dasd5asd4as8d7asdasd56asd7as8d7asda5s8d7asd8s5d 192.168.1.20 7846 125 485 25";
      dout.writeUTF(msg);
      dout.flush();

      din.close();
      dout.close();

      System.out.println("wrote\t" + msg);

    } catch (GeneralSecurityException | IOException gse) {
      gse.printStackTrace();
    }
  }

  public static void main(String[] args) {
    CentralServerTest test = new CentralServerTest();

    test.connect("localhost", 8585);
  }
}
