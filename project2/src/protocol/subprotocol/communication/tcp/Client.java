package protocol.subprotocol.communication.tcp;

import protocol.Chunk;
import server.SSLInit;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import static protocol.subprotocol.fileManagement.FileManager.MAX_CHUNK_SIZE;

public class Client extends SSLInit {

    private SSLSocket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    private int PORT;
    private InetAddress address;

    public Client(String port, String address) {
        super("clientpw");
        try {
            this.PORT = Integer.parseInt(port);
            this.address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void startConnection() throws IOException {
        String addr = "localhost";
        if(!address.toString().equalsIgnoreCase(addr)) {
            addr = address.toString().split("/")[1];
        }
        System.out.println("address: " + addr );
        System.out.println("port: " + PORT );
        clientSocket = initClient(addr, PORT);
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    private void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    private List<Chunk> connect(String peerID) {
        LinkedList<Chunk> chunks = new LinkedList<>();
        boolean completed = false;

        while (!completed) {
            try {
                int chunkNo = in.readInt();
                System.out.println("Received chunk NO " + chunkNo);
                int length = in.readInt();
                System.out.println("Received chunk length " + length);
                byte[] body = new byte[length];
                //int size = in.read(body, 0, length);
                int size = length;
                        in.readFully(body);
                if (size != length) //error
                {
                    size = in.read(body, size+1, length-size);
                    System.out.println("Received chunk size " + size);
                   // return null;
                }
                Chunk chunk = new Chunk(chunkNo, body, peerID);
                chunks.add(chunk);

                if (length < MAX_CHUNK_SIZE) {
                    completed = true;
                    System.out.println("Finished reading chunks ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return chunks;
    }

    public List<Chunk> receiveChunk(String peerID) {
        try {
            startConnection();
            List<Chunk> chunks = connect(peerID);
            stopConnection();
            return chunks;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
