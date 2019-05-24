package connection.client;

import connection.Connection;
import connection.client.handler.Handler;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Connection implements Runnable {

    public static final String message =
            "The first step toward success is taken when you refuse to be a captive of the environment in which you first find yourself.";

    private InetAddress server_address;
    private int server_port;

    private Socket socket;

    public Client(int port) throws IOException {
        server_address = InetAddress.getLocalHost();
        server_port = port;

        startConnection();
    }

    public Client(InetAddress address, int port) throws IOException {
        server_address = address;
        server_port = port;

        startConnection();
    }

    private void startConnection() throws IOException {
        socket = createSocket(this.server_address.getHostAddress(), this.server_port);
        is = new BufferedInputStream(socket.getInputStream());
        os = new BufferedOutputStream(socket.getOutputStream());
    }

    private Socket createSocket(String host, int port) throws IOException {
        System.out.printf("Client started on port %d%n", port);
        SSLSocketFactory sslFact = (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket)sslFact.createSocket(host, port);
        socket.setEnabledProtocols(protocols);
        socket.setEnabledCipherSuites(cipher_suites);
        return socket;
    }

    @Override
    public void run() {
        try {
            byte[] data = new byte[BUFF_SIZE];
            Handler handler = new Handler();
            while(true) {
                int len = is.read(data);
                if (len <= 0) {
                    throw new IOException("no data received");
                }
                String message = new String(data, 0, len);
                System.out.printf("client received %d bytes: %s%n", len, message);
                handler.handle(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
