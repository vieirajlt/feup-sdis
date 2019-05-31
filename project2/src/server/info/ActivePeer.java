package server.info;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import server.IncomingConnection;

public class ActivePeer {
  private long freeSpace;

  private DataInputStream input;
  private DataOutputStream output;

  private String host;
  private String port;

  private boolean busy = false;

  public ActivePeer(long freeSpace, IncomingConnection connection) {
    this.freeSpace = freeSpace;

    this.input = connection.getInputStream();
    this.output = connection.getOutputStream();

    this.host = connection.getHost();
    this.port = connection.getPort();
  }

  public DataInputStream getInput() {
    return input;
  }

  public DataOutputStream getOutput() {
    return output;
  }

  public long getFreeSpace() {
    return freeSpace;
  }

  public void addFreeSpace(long space) {
    this.freeSpace += space;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public boolean enoughSpace(long fileSize) {
    return this.freeSpace >= fileSize;
  }

  synchronized public boolean isBusy() {
    return busy;
  }

  synchronized public void setBusy(boolean busy) {
    this.busy = busy;
  }
}
