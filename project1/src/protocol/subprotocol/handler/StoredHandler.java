package protocol.subprotocol.handler;

import protocol.Peer;

import protocol.Chunk;

import static protocol.subprotocol.Subprotocol.STORED;

public class StoredHandler extends Handler implements Runnable {

    private String fileId;
    private int chunkNo;

    public StoredHandler(String fileId, int chunkNo) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        String chunkId = Chunk.buildChunkId(fileId, chunkNo);
        Peer.getDataContainer().addPeerChunk(chunkId);

        byte[] message = buildMessage(STORED, MSG_CONFIG_STORED, fileId, chunkNo, -1, null);

        try {
            Thread.sleep(getSleep_time_ms());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Peer.getControlChannel().write(message);
    }
}
