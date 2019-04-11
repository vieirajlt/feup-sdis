package protocol.subprotocol;

import protocol.Peer;

public abstract class Subprotocol {

    public final static String BACKUP_SUBPROTOCOL = "BACKUP_SUBPROTOCOL";
    public final static String RESTORE_SUBPROTOCOL = "RESTORE_SUBPROTOCOL";
    public final static String DELETE_SUBPROTOCOL = "DELETE_SUBPROTOCOL";
    public final static String RECLAIM_SUBPROTOCOL = "RECLAIM_SUBPROTOCOL";
    public final static String STATE_SUBPROTOCOL = "STATE_SUBPROTOCOL";

    public final static String PUTCHUNK = "PUTCHUNK";
    public final static String GETCHUNK = "GETCHUNK";
    public final static String CHUNK = "CHUNK";
    public final static String REMOVED = "REMOVED";
    public final static String DELETE = "DELETE";
    public final static String STORED = "STORED";

    public final static String FILESTATUS = "FILESTATUS";

    protected boolean isEnhancementAllowed(String protocol) {
        if (Peer.getProtocolVersion().equals("2.0"))
            return true;
        else if (Peer.getProtocolVersion().equals("1.1") && (protocol.equals(PUTCHUNK) || protocol.equals(STORED) || protocol.equals(BACKUP_SUBPROTOCOL)))
            return true;
        else if (Peer.getProtocolVersion().equals("1.2") && (protocol.equals(GETCHUNK) || protocol.equals(CHUNK) || protocol.equals(RESTORE_SUBPROTOCOL)))
            return true;
        else if (Peer.getProtocolVersion().equals("1.3") && (protocol.equals(DELETE) || protocol.equals(FILESTATUS) || protocol.equals(DELETE_SUBPROTOCOL)))
            return true;
        return false;
    }

}
