package protocol.subprotocol;

import protocol.Peer;

public abstract class Subprotocol {

    public final static String PUTCHUNK = "PUTCHUNK";
    public final static String GETCHUNK = "GETCHUNK";
    public final static String CHUNK = "CHUNK";
    public final static String REMOVED = "REMOVED";
    public final static String DELETE = "DELETE";

    public final static String STORED = "STORED";



    public abstract boolean run(String message);

    protected abstract void backup(String[] cmd);

    protected abstract void restore(String[] cmd);

    protected abstract void delete(String[] cmd);

    protected abstract void reclaim(String[] cmd);

}
