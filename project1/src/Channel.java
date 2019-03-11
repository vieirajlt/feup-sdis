import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Channel implements Runnable {

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
                String received = new String(packet.getData());
                System.out.println("Echoed Message: " + received);
                int indexCut = received.indexOf(0);
                String receivedCut = received.substring(0, indexCut);
                Peer.runProtocol(receivedCut);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] sbuf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, PORT);
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
