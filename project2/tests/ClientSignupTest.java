import protocol.Chunk;
import protocol.subprotocol.communication.tcp.Client;
import server.ClientSocket;

public class ClientSignupTest {

  public static void main(String[] args) {
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    String peerID = args[2];

    ClientSocket test = new ClientSocket(host, port);

    test.write("SIGNUP " + peerID + " " + Math.round(100000 + Math.random() * 600000));

    while (true) {
      try {

        String msg = test.read();

        if (msg != null) {
          String header = msg.split(" ")[0];

          if (header.equalsIgnoreCase("backup")) test.write("Accepted");
          else if (header.equalsIgnoreCase("receive")) {

            host = msg.split(" ")[1];
            String sPort = msg.split(" ")[2];

            Client client = new Client(sPort, host);
            System.out.println("Received " + msg);
            Chunk chunk = client.receiveChunk();

            System.out.println("Received chunk " + chunk.getSize());

          } else System.out.println(msg);
        } else {
          System.out.println("NULL");
        }
      } catch (Exception e) {
        System.err.println("Error");
        e.printStackTrace();
      }
    }
  }
}
