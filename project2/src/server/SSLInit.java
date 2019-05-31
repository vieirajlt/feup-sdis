package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLInit {

  private SecureRandom secureRandom = new SecureRandom();

  private String passphrase;

  private KeyStore cltKeyStore;

  private KeyStore srvKeyStore;

  private SSLContext sslContext;

  protected SSLInit(String passphrase) {
    this.passphrase = passphrase;
  }

  protected SSLInit() {
    this.passphrase = "";
  }



  protected SSLServerSocket initServer(int port) {
    try {
      setupClientKeyStore(true);
      setupServerKeystore(true);
      setupSSLContext(true);

      SSLServerSocketFactory sf = sslContext.getServerSocketFactory();
      return (SSLServerSocket) sf.createServerSocket(port);

    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  protected SSLSocket initClient(String host, int port) {
    try {
      setupClientKeyStore(false);
      setupServerKeystore(false);
      setupSSLContext(false);

      SSLSocketFactory sf = sslContext.getSocketFactory();
      return (SSLSocket) sf.createSocket(host, port);

    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

    return null;
  }


  private void setupServerKeystore(boolean server) throws GeneralSecurityException, IOException {
    srvKeyStore = KeyStore.getInstance("JKS");
    if (server) {
      srvKeyStore.load(new FileInputStream("../certs/server.private"), passphrase.toCharArray());
    } else {
      srvKeyStore.load(new FileInputStream("../certs/server.public"), "public".toCharArray());
    }
  }

  private void setupClientKeyStore(boolean server) throws GeneralSecurityException, IOException {
    cltKeyStore = KeyStore.getInstance("JKS");
    if (server) {
      cltKeyStore.load(new FileInputStream("../certs/client.public"), "public".toCharArray());
    } else {
      cltKeyStore.load(new FileInputStream("../certs/client.private"), passphrase.toCharArray());
    }
  }

  private void setupSSLContext(boolean server) throws GeneralSecurityException {
    secureRandom.nextInt();

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    if (server) {
      tmf.init(cltKeyStore);
    } else {
      tmf.init(srvKeyStore);
    }

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    if (server) {
      kmf.init(srvKeyStore, passphrase.toCharArray());
    } else {
      kmf.init(cltKeyStore, passphrase.toCharArray());
    }

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
  }


}
