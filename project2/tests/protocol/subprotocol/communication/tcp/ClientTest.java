package protocol.subprotocol.communication.tcp;

import protocol.Chunk;

public class ClientTest {
  public static void main(String[] args) {
    Client client = new Client(args[0], args[1]);

    Chunk chunk = client.receiveChunk();

    System.out.println(new String(chunk.getBody()));
  }
}
