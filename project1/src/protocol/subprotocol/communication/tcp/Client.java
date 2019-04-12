package protocol.subprotocol.communication.tcp;

import protocol.Chunk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    private int PORT;
    private InetAddress address;

    public Client(String port, String address) {
        try {
            this.PORT = Integer.parseInt(port);
            this.address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void startConnection() throws IOException {
        clientSocket = new Socket(address, PORT);
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    private void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    private Chunk connect() {
        try {
            int chunkNo = in.readInt();
            int length = in.readInt();
            byte[] body = in.readNBytes(length);
            Chunk chunk = new Chunk(chunkNo, body);
            return chunk;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Chunk receiveChunk() {
        try {
            startConnection();
            Chunk chunk = connect();
            stopConnection();
            return chunk;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
