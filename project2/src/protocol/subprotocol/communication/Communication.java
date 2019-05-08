package protocol.subprotocol.communication;

public interface Communication extends Runnable {

    public void read();
    public void write(byte[] message);

}
