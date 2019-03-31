package protocol.subprotocol.handler;

import protocol.Peer;

import protocol.Chunk;

import static protocol.subprotocol.Subprotocol.STORED;

public class StoredHandler extends Handler implements Runnable {

    private String fileId;
    private int chunkNo;
    private int repDegree;

    public StoredHandler(String fileId, int chunkNo, int repDegree) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.repDegree = repDegree;
    }

    @Override
    public void run() {
        String chunkId = Chunk.buildChunkId(fileId, chunkNo);
        Peer.getDataContainer().addPeerChunk(chunkId);
        Peer.getDataContainer().addBackedUpChunk(chunkId,repDegree);

        byte[] message = buildMessage(STORED, MSG_CONFIG_STORED, fileId, chunkNo, -1, null);

        try {
            Thread.sleep(getSleep_time_ms());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Peer.getControlChannel().write(message);
    }
}
