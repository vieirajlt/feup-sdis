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

class SSLInit {

  /**
   * KeyStore for storing our public/private key pair
   */
  private KeyStore clientKeyStore;

  /**
   * KeyStore for storing the server's public key
   */
  private KeyStore serverKeyStore;

  /**
   * Used to generate a SocketFactory
   */
  private SSLContext sslContext;

  /**
   * A source of secure random numbers
   */
  private SecureRandom secureRandom = new SecureRandom();

  private String passphrase;

  SSLInit(String passphrase) {
    this.passphrase = passphrase;
  }

  private void setupClientKeyStore(boolean server) throws GeneralSecurityException, IOException {
    clientKeyStore = KeyStore.getInstance("JKS");
    if (server) {
      clientKeyStore.load(new FileInputStream("./certs/client.public"), "public".toCharArray());
    } else {
      clientKeyStore.load(new FileInputStream("./certs/client.private"), passphrase.toCharArray());
    }
  }

  private void setupServerKeystore(boolean server) throws GeneralSecurityException, IOException {
    serverKeyStore = KeyStore.getInstance("JKS");
    if (server) {
      serverKeyStore.load(new FileInputStream("./certs/server.private"), passphrase.toCharArray());
    } else {
      serverKeyStore.load(new FileInputStream("./certs/server.public"), "public".toCharArray());
    }
  }

  private void setupSSLContext(boolean server) throws GeneralSecurityException {
    secureRandom.nextInt();

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    if (server) {
      tmf.init(clientKeyStore);
    } else {
      tmf.init(serverKeyStore);
    }

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    if (server) {
      kmf.init(serverKeyStore, passphrase.toCharArray());
    } else {
      kmf.init(clientKeyStore, passphrase.toCharArray());
    }

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
  }

  SSLSocket initClient(String host, int port) {
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

  SSLServerSocket initServer(int port) {
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
}
