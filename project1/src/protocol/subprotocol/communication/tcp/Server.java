package protocol.subprotocol.communication.tcp;

import protocol.Chunk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    private int PORT;
    private InetAddress address;

    private Chunk chunk;

    public Server(Chunk chunk) {
        PORT = pickRandomPort();
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.chunk = chunk;
    }

    private void startConnection() throws IOException {
        boolean connected = false;
        while(!connected) {
            try {
                serverSocket = new ServerSocket(PORT);
                connected = true;
            } catch (IOException e) {
                PORT = pickRandomPort();
            }
        }
        clientSocket = serverSocket.accept();
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    private int pickRandomPort() {
        Random r = new Random();
        return r.nextInt((65535 - 49152) + 1);
    }

    private void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    private void connect(Chunk chunk) {
        try {
            out.writeInt(chunk.getChunkNo());
            out.writeInt(chunk.getBody().length);
            out.write(chunk.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConnectionSettings() {
        return address.getHostName() + ":" + PORT;
    }

    public void sendChunk() {
        try {
            startConnection();
            connect(chunk);
            stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
