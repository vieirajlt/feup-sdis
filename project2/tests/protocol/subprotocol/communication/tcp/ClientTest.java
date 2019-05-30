package protocol.subprotocol.communication.tcp;

import protocol.Chunk;

import java.util.List;

public class ClientTest {
    public static void main(String[] args) {
        Client client = new Client(args[0], args[1]);

        List<Chunk> chunks = client.receiveChunk("1");

        for (Chunk chunk : chunks)
            System.out.println(new String(chunk.getBody()));
    }
}
