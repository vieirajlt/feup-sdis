
import server.ClientSocket;
import sun.security.provider.SHA2;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ClientDeleteTest {

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String filename = args[2];

        ClientSocket test = new ClientSocket(host, port);

        String hashedName = "";


        test.write("DELETE " + hashedName);
        System.out.println("WROTE " + "DELETE " + hashedName);

    }
}
