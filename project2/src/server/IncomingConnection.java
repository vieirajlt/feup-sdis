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

  IncomingConnection(Socket so, CentralServer se) {
    try {
      InputStream in = so.getInputStream();
      OutputStream out = so.getOutputStream();

      this.inputStream = new DataInputStream(in);
      this.outputStream = new DataOutputStream(out);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    System.out.println("START INCOMING CONNECTION");
    try {
      String msg = this.inputStream.readUTF();

      se.request(msg);

      System.out.println(msg);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
