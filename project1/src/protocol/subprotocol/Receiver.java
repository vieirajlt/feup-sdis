package protocol.subprotocol;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.action.ChunkAction;
import protocol.subprotocol.action.DeleteAction;
import protocol.subprotocol.action.RemovedAction;
import protocol.subprotocol.action.StoredAction;
import protocol.subprotocol.communication.tcp.Client;
import protocol.subprotocol.handler.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Receiver extends Subprotocol implements Runnable{

    private byte[] message;

    public Receiver(byte[] byteMessage) {
        message = byteMessage;
    }

    @Override
    public void run() {

        ArrayList<byte[]> separator = headerBodySeparator(message);

        String header = new String(separator.get(0), StandardCharsets.UTF_8);
        byte[] body = separator.get(1);

        String[] cmd = header.split(" ");

        if (!checkHeader(cmd) && !cmd[0].equals(DELETE))
            return;

        if (cmd[0].equalsIgnoreCase(PUTCHUNK)) {
            putchunk(cmd, body);
        } else if (cmd[0].equalsIgnoreCase(GETCHUNK)) {
            getchunk(cmd);
        } else if (cmd[0].equalsIgnoreCase(REMOVED)) {
            removed(cmd);
        } else if (cmd[0].equalsIgnoreCase(DELETE)) {
            delete(cmd);
        } else if (cmd[0].equalsIgnoreCase(STORED)) {
            stored(cmd);
        } else if (cmd[0].equalsIgnoreCase(CHUNK)) {
            chunk(cmd, body);
        } else if (cmd[0].equalsIgnoreCase(FILESTATUS)) {
            filestatus(cmd, body);
        } else if (cmd[0].equalsIgnoreCase(STATUS)) {
            status(cmd, body);
        }

    }

    private synchronized void putchunk(String[] header, byte[] body) {
        System.out.println("protocol.subprotocol.Receiver.putchunk");

        int senderId = Integer.parseInt(header[2]);
        int chunkNo = Integer.parseInt(header[4]);
        Chunk chunk = new Chunk(chunkNo, body);

        String fileId = header[3];

        int repDegree = Integer.parseInt(header[5]);
        float completionPercentage = 1;
        //case of enhancement
        if(header.length > 7) {
            completionPercentage = Float.parseFloat(header[6]);
        }

        StoredHandler handler = new StoredHandler(senderId, fileId, chunk, repDegree, completionPercentage);
        handler.handle();
    }

    private synchronized void getchunk(String[] header) {
        System.out.println("protocol.subprotocol.Receiver.getchunk");

        boolean enhanced = false;
        //case of enhancement
        if(header.length > 6 && header[5].equals("ENH"))
            enhanced = true;

        int chunkNo = Integer.parseInt(header[4]);
        String fileId = header[3];

        ChunkHandler handler = new ChunkHandler(fileId, chunkNo, enhanced); //CHUNK
        handler.handle();
    }

    private synchronized void delete(String[] header) {
        System.out.println("protocol.subprotocol.Receiver.delete");
        String fileId = header[3];

        DeleteAction action = new DeleteAction(fileId);
        action.process();
    }

    private synchronized void removed(String[] header) {
        System.out.println("protocol.subprotocol.Receiver.removed");

        String senderId = header[2];
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);
        Chunk chunk = new Chunk(chunkNo);
        String chunkKey = chunk.buildChunkKey(fileId);

        //for needed putchunk messages
        //consider enhanced if Peer version allows it
        boolean enhanced = isEnhancementAllowed(BACKUPENH);

        RemovedAction action = new RemovedAction(senderId, fileId, chunkKey, chunkNo, enhanced);
        action.process();
    }

    private synchronized void stored(String[] header) {
        String senderId = header[2];
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        System.out.println("protocol.subprotocol.Receiver.stored from " + senderId + " of chunkNo " + chunkNo);

        StoredAction action = new StoredAction(senderId, fileId, chunkNo);
        action.process();
    }

    private synchronized void chunk(String[] header, byte[] body) {
        System.out.println("protocol.subprotocol.Receiver.chunk");
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        //case of enhancement
        //connection info sent ending header
        boolean enhanced = false;
        if(header.length > 6)
            enhanced = true;

        byte[] chunkBody = body;

        if(enhanced) {
            //if peer is the owner of the file
            if (Peer.getDataContainer().getOwnFile(fileId) != null) {
                String[] connection = header[5].split(":");
                Client client = new Client(connection[1], connection[0]);
                Chunk chunk = client.receiveChunk();
                if (chunk == null)
                    return;

                if (chunkNo != chunk.getChunkNo())
                    System.out.println("diff ChunkNo");

                chunkNo = chunk.getChunkNo();
                chunkBody = chunk.getBody();
            }
        }

        ChunkAction action = new ChunkAction(fileId, chunkBody, chunkNo);
        action.process();

    }

    //<FILESTATUS> <SenderId> <FileId> <FileOwnerId> <CRLF><CRLF>
    private synchronized void filestatus(String[] header, byte[] body) {
        System.out.println("protocol.subprotocol.Receiver.filestatus");

        int fileOwnerId = Integer.parseInt(header[4]);

        //if peer is not the owner of the file
        if(fileOwnerId != Peer.getServerId())
            return;

        String fileId = header[3];

        int status = 1;

        //if the file was not deleted status = 0, else status = 1
        if(Peer.getDataContainer().getOwnFile(fileId) != null)
            status = 0;

        StatusHandler handler = new StatusHandler(fileId, status);
        handler.handle();
    }

    //<STATUS> <SenderId> <FileId> <Status> <CRLF><CRLF>
    private synchronized void status(String[] header, byte[] body) {
        System.out.println("protocol.subprotocol.Receiver.status");

        int senderId = Integer.parseInt(header[2]);

        String fileId = header[3];

        //does not backup the file
        if(!Peer.getDataContainer().hasTmpBackedUpFile(fileId, senderId))
            return;

        int status = Integer.parseInt(header[4]);

        Peer.getDataContainer().setTmpBackedUpFileResponse(fileId,senderId, status);
        //the file was not deleted
        if(status == 0)
            return;  //the file was deleted
        else if(status == 1){
            DeleteHandler handler = new DeleteHandler(fileId);
            handler.handle();
        }
    }

    private boolean checkHeader(String[] header) {
        return header[1].equals(Peer.getProtocolVersion())
                && Integer.parseInt(header[2]) != Peer.getServerId();
    }

    private ArrayList<byte[]> headerBodySeparator(byte[] byteMessage) {
        ArrayList<byte[]> separator = new ArrayList<>();

        for (int index = 0; index < byteMessage.length; ++index) {
            if (byteMessage[index] == Handler.CR &&
                    byteMessage[index + 1] == Handler.LF &&
                    byteMessage[index + 2] == Handler.CR &&
                    byteMessage[index + 3] == Handler.LF) {
                index += 4;
                byte[] header = Arrays.copyOfRange(byteMessage, 0, index);
                byte[] body = Arrays.copyOfRange(byteMessage, index, byteMessage.length);
                separator.add(header);
                separator.add(body);
                return separator;
            }
        }
        return null;
    }
}
