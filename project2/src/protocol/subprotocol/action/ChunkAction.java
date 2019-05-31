package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.fileManagement.RestoreFile;

import java.util.ArrayList;

public class ChunkAction extends Action {

    private String fileId;
    private byte[] body;
    private int chunkNo;

    public ChunkAction(String fileId, byte[] body, int chunkNo) {
        this.fileId = fileId;
        this.body = body;
        this.chunkNo = chunkNo;
    }

    @Override
    public void process() {
        ArrayList<Chunk> chunks = Peer.getDataContainer().getTmpChunksChunks(fileId);

        //if not initialized, start it full of nulls with the required size
        if (chunks == null) {
            if (Peer.getDataContainer().getOwnFile(fileId) == null)
                return;
            int chunksSize = Peer.getDataContainer().getOwnFileNrOfChunks(fileId);
            Peer.getDataContainer().iniTmpChunksChunks(fileId, chunksSize);
            chunks = Peer.getDataContainer().getTmpChunksChunks(fileId);
        }

        //if chunk already received, ignore it
        if (chunks.get(chunkNo) == null) {
            Chunk chunk = new Chunk(chunkNo, body);
            chunks.set(chunkNo, chunk);

            //if all chunks received, start getchunk
            if (Peer.getDataContainer().isTmpChunksChunksComplete(fileId)) {
                RestoreFile restoreFile = new RestoreFile(fileId);
                restoreFile.process(null, null);
            }
        }
    }
}
