package protocol.subprotocol;

import protocol.Peer;

public abstract class Subprotocol {

    public final static String BACKUPENH = "BACKUPENH";
    public final static String RESTOREENH = "RESTOREENH";
    public final static String DELETEENH = "DELETEENH";

    public final static String PUTCHUNK = "PUTCHUNK";
    public final static String GETCHUNK = "GETCHUNK";
    public final static String CHUNK = "CHUNK";
    public final static String REMOVED = "REMOVED";
    public final static String DELETE = "DELETE";
    public final static String STORED = "STORED";

    public final static String FILESTATUS = "FILESTATUS";
    public final static String STATUS = "STATUS";

    protected static boolean isEnhancementAllowed(String protocol) {
        if (protocol.equals(BACKUPENH)) {
            if (Peer.getProtocolVersion().equals("1.1") || Peer.getProtocolVersion().equals("2.0"))
                return true;
        } else if (protocol.equals(RESTOREENH)) {
            if (Peer.getProtocolVersion().equals("1.2") || Peer.getProtocolVersion().equals("2.0"))
                return true;
        } else if (protocol.equals(DELETEENH)) {
            if (Peer.getProtocolVersion().equals("1.3") || Peer.getProtocolVersion().equals("2.0"))
                return true;
        }
        return false;
    }

}
