package protocol.subprotocol;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.action.ChunkAction;
import protocol.subprotocol.action.DeleteAction;
import protocol.subprotocol.action.RemovedAction;
import protocol.subprotocol.action.StoredAction;
import protocol.subprotocol.handler.ChunkHandler;
import protocol.subprotocol.handler.Handler;
import protocol.subprotocol.handler.StoredHandler;

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

        if (!checkHeader(cmd))
            return;

        if (cmd[0].equals(PUTCHUNK)) {
            putchunk(cmd, body);
        } else if (cmd[0].equals(GETCHUNK)) {
            getchunk(cmd);
        } else if (cmd[0].equals(REMOVED)) {
            removed(cmd);
        } else if (cmd[0].equals(DELETE)) {
            delete(cmd);
        } else if (cmd[0].equals(STORED)) {
            stored(cmd);
        } else if (cmd[0].equals(CHUNK)) {
            chunk(cmd, body);
        }
    }

    private synchronized void putchunk(String[] header, byte[] body) {
        System.out.println("protocol.subprotocol.Receiver.putchunk");

        int chunkNo = Integer.parseInt(header[4]);
        Chunk chunk = new Chunk(chunkNo, body);

        String fileId = header[3];

        int repDegree = Integer.parseInt(header[5]);

        StoredHandler handler = new StoredHandler(fileId, chunk, repDegree);
        handler.handle();
    }

    private synchronized void getchunk(String[] header) {
        System.out.println("protocol.subprotocol.Receiver.getchunk");

        //TODO what to do with this??
        String senderId = header[2];

        int chunkNo = Integer.parseInt(header[4]);
        String fileId = header[3];

        ChunkHandler handler = new ChunkHandler(fileId, chunkNo); //CHUNK
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

        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);
        Chunk chunk = new Chunk(chunkNo);
        String chunkKey = chunk.buildChunkKey(fileId);

        RemovedAction action = new RemovedAction(fileId, chunkKey, chunkNo);
        action.process();
    }

    private synchronized void stored(String[] header) {
        String senderId = header[2];
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        System.out.println("protocol.subprotocol.Receiver.stored from " + senderId + " of chunkNo " + chunkNo);

        StoredAction action = new StoredAction(fileId, chunkNo);
        action.process();
    }

    private synchronized void chunk(String[] header, byte[] body) {
        System.out.println("protocol.subprotocol.Receiver.chunk");
        //TODO what to do with this??
        String senderId = header[2];
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        ChunkAction action = new ChunkAction(fileId, body, chunkNo);
        action.process();

    }

    private boolean checkHeader(String[] header) {
        return Float.parseFloat(header[1]) == Peer.getProtocolVersion()
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
