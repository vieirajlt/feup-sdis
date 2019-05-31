
import protocol.subprotocol.fileManagement.SplitFile;
import server.ClientSocket;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ClientDeleteTest {

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String filename = args[2];

        ClientSocket test = new ClientSocket(host, port);

        String hashedName = SplitFile.sha256(filename);

        test.write("DELETE " + hashedName);
        System.out.println("WROTE " + "DELETE " + hashedName);

    }
}
