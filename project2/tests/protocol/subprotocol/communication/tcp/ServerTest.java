package protocol.subprotocol.communication.tcp;

import protocol.Chunk;

public class ServerTest {
  public static void main(String[] args) {
    String msg = "Hey, this is a message";

    Chunk chunk = new Chunk(0, msg.getBytes());
    Server server = new Server(chunk);

    System.out.println(server.getConnectionSettings());

    server.sendChunk();
  }
}
