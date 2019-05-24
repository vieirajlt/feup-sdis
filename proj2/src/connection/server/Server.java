package connection.server;

import connection.server.handler.Handler;
import connection.Connection;

import javax.net.ssl.*;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server extends Connection implements Runnable, AutoCloseable {

    private static final int FREE_PORT = 0;

    private final SSLServerSocket sslServerSocket;

    public Server(SSLServerSocket sslsocket) {
        this.sslServerSocket = sslsocket;
    }

    public static Server create() throws IOException {
        return create(FREE_PORT);
    }

    public static Server create(int port) throws IOException {
        SSLServerSocketFactory sslSrvFact = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        SSLServerSocket sslSrvSocket = (SSLServerSocket)sslSrvFact.createServerSocket(port);
        sslSrvSocket.setEnabledProtocols(protocols);
        sslSrvSocket.setEnabledCipherSuites(cipher_suites);
        sslSrvSocket.setNeedClientAuth(false);
        return new Server(sslSrvSocket);
    }

    public int getLocalPort() {
        return sslServerSocket.getLocalPort();
    }

    public String getAddr() {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {

        try (SSLSocket socket = (SSLSocket) sslServerSocket.accept()) {
            System.out.println("accepted");
            is = new BufferedInputStream(socket.getInputStream());
            os = new BufferedOutputStream(socket.getOutputStream());
            byte[] data = new byte[BUFF_SIZE];
            Handler handler = new Handler();
            while(true) {
                int len = is.read(data);
                if (len <= 0) {
                    throw new IOException("no data received");
                }
                String message = new String(data, 0, len);
                System.out.printf("server received %d bytes: %s%n", len, message);
                handler.handle(message);
            }
        } catch (Exception e) {
            System.out.printf("exception: %s%n", e.getMessage());
        }

        System.out.println("server stopped");
    }

    @Override
    public void close() throws Exception {
        if (sslServerSocket != null && !sslServerSocket.isClosed()) {
            sslServerSocket.close();
        }
    }
}
