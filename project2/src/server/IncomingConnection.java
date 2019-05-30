package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class IncomingConnection implements Runnable {

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private CentralServer se;
    private Socket socket;

    IncomingConnection(Socket so, CentralServer se) {
        try {
            InputStream in = so.getInputStream();
            OutputStream out = so.getOutputStream();

            this.inputStream = new DataInputStream(in);
            this.outputStream = new DataOutputStream(out);

            this.se = se;
            this.socket = so;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {

            String msg = this.inputStream.readUTF();

            se.request(msg, this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public DataInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public String getHost() {
        return this.socket.getInetAddress().getHostAddress();
    }

    public String getPort() {
        return Integer.toString(this.socket.getPort());
    }
}
