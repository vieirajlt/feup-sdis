import server.ClientSocket;

public class ClientSignupTest {

  public static void main(String[] args) {
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    String peerID = args[2];

    ClientSocket test = new ClientSocket(host, port);

    test.write("SIGNUP " + peerID + " " + Math.round(100000 + Math.random() * 600000));

    while (true) {
      String msg = test.read();

      if (msg != null) {
        if (msg.split(" ")[0].equalsIgnoreCase("backup")) test.write("Accepted");
        else System.out.println(msg);
      } else {
        System.out.println("NULL");
      }
    }
  }
}
