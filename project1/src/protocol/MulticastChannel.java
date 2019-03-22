package protocol;

import protocol.subprotocol.FileManagement.FileManager;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class MulticastChannel implements Runnable{

    private int PORT;
    private InetAddress address;

    public MulticastChannel(String address, String port) {
        try {
            this.PORT = Integer.parseInt(port);
            this.address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        byte[] buf = new byte[FileManager.MAX_CHUNK_SIZE + 500];

        try (MulticastSocket clientSocket = new MulticastSocket(PORT)) {
            clientSocket.joinGroup(address);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                clientSocket.receive(packet);
                byte[] data = Arrays.copyOf( packet.getData(), packet.getLength());
                Peer.answerProtocol(data);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void write(byte[] message) {
        try (DatagramSocket serverSocket = new DatagramSocket()) {

            DatagramPacket msgPacket = new DatagramPacket(message,
                    message.length, address, PORT);
            serverSocket.send(msgPacket);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        read();
    }
}
