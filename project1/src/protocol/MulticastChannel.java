package protocol;

import java.io.IOException;
import java.net.*;

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
        byte[] buf = new byte[SplitFile.MAX_CHUNK_SIZE + 500];

        try (MulticastSocket clientSocket = new MulticastSocket(PORT)) {
            clientSocket.joinGroup(address);

            while (true) {
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);

                String msg = new String(buf, 0, buf.length);

                //Should run appropriate cmd on protocol.Peer
                System.out.println("READ: " + msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void write(String message) {
        try (DatagramSocket serverSocket = new DatagramSocket()) {

            DatagramPacket msgPacket = new DatagramPacket(message.getBytes(),
                    message.getBytes().length, address, PORT);
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
