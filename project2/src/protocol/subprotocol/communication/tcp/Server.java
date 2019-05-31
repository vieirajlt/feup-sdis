package protocol.subprotocol.communication.tcp;

import protocol.Chunk;
import server.SSLInit;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

public class Server extends SSLInit {

    private SSLServerSocket serverSocket;
    private SSLSocket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    private int PORT;
    private InetAddress address;

    private Chunk chunk;
    private List<Chunk> chunks;

    public Server(Chunk chunk) {
        super("serverpw");
        PORT = pickRandomPort();
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.chunk = chunk;
    }

    public Server(List<Chunk> chunks) {
        super("serverpw");
        PORT = pickRandomPort();
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.chunks = chunks;
        this.chunk = null;
    }

    private void startConnection() throws IOException {
        serverSocket = initServer(PORT);
        clientSocket = (SSLSocket) serverSocket.accept();
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    private int pickRandomPort() {
        Random r = new Random();
        return r.nextInt((65535 - 49152) + 1);
    }

    private void stopConnection() throws IOException {
        System.out.println("Stopped connection");
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    private void connect(Chunk chunk) {
        try {
            System.out.println("connect no "+ chunk.getChunkNo());
            out.writeInt(chunk.getChunkNo());
            System.out.println("connect body length "+ chunk.getBody().length);
            out.writeInt(chunk.getBody().length);
            out.write(chunk.getBody());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConnectionSettings() {
        return address.getHostAddress() + ":" + PORT;
    }

    public void sendChunk() {
        try {
            startConnection();
            if (chunk == null) {
                for(Chunk c : chunks) {
                    connect(c);
                    Thread.sleep((long) (Math.random() * 400 + 100));
                }
            } else {
                connect(chunk);
                Thread.sleep((long) (Math.random() * 400 + 100));
            }

            stopConnection();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
