import server.ClientSocket;

public class ClientBackupTest {

  public static void main(String[] args) {
    String host = args[0];
    int port = Integer.parseInt(args[1]);

    ClientSocket test = new ClientSocket(host, port);

    test.write("BACKUP 1as98d21hiwdhwadh19832rhwqi 3 45000");

    while (true) {
      String msg = test.read();

      System.out.println(msg);
    }
  }
}
