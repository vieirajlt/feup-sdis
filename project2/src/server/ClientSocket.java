package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.net.ssl.SSLSocket;

public class ClientSocket extends SSLInit {

  private DataInputStream din;
  private DataOutputStream dout;

  public ClientSocket(String host, int port) {
    super("clientpw");
    this.connect(host, port);
  }

  private void connect(String host, int port) {
    try {

      SSLSocket socket = initClient(host, port);

      if (socket == null) {
        System.err.println("Error creating SSLSocket");
        return;
      }

      InputStream in = socket.getInputStream();
      OutputStream out = socket.getOutputStream();

      din = new DataInputStream(in);
      dout = new DataOutputStream(out);

    } catch (IOException gse) {
      gse.printStackTrace();
    }
  }

  public void write(String msg) {
    try {
      dout.writeUTF(msg);
      dout.flush();

      System.out.println("wrote\t" + msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String read() {
    try {
      String msg = din.readUTF();
      return msg;
    } catch (IOException e) {
     // e.printStackTrace();
    }

    return null;
  }

  public void close(){
    try{
      din.close();
      dout.close();
    }catch(IOException e){
      e.printStackTrace();
    }
  }
}
