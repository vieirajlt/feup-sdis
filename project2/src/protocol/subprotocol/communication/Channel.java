package protocol.subprotocol.communication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Channel implements Communication {

    private int PORT;
    private InetAddress address;

    private int MAX_BUFFER_SIZE = 500;

    public Channel(String address, String port) {
        try {
            this.PORT = Integer.parseInt(port);
            this.address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] rbuf = new byte[MAX_BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);

            while(true) {
                socket.receive(packet);
                byte[] data = Arrays.copyOf( packet.getData(), packet.getLength());
                //String receivedCut = received.substring(0, indexCut);
                //Peer.initiateProtocol(data, acceptText);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(message, message.length, address, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        read();
    }
}
