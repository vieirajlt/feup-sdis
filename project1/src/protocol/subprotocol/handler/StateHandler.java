package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.ChunkInfo;
import protocol.FileInfo;
import protocol.Peer;

import java.io.File;
import java.util.HashMap;

public class StateHandler extends Handler{

    public StateHandler() {
    }

    @Override
    public void handle() {

        String stateInfo = "Own Files:\n";

        //for each file initiated
        for (HashMap.Entry<String, FileInfo> entry : Peer.getDataContainer().getOwnFiles().entrySet()) {
            //file pathname
            String pathname = entry.getValue().getName();
            //backup service fileId
            String fileId = entry.getKey();
            //desirable repDegree
            int desRepDegree = entry.getValue().getRepDegree();
            //for each chunk, id and currRepDegree
            String chunksInfo = "";
            for (int i = 0; i < entry.getValue().getNrOfChunks(); ++i) {
                String chunkId = Chunk.buildChunkId(i);
                String chunkKey = Chunk.buildChunkKey(fileId, i);
                int currRepDegree = Peer.getDataContainer().getStoredCurrRepDegree(chunkKey);
                chunksInfo += "\t\tID: " + chunkId + "\tPerceived Replication Degree: " + currRepDegree + "\n";
            }
            stateInfo += "\tPathname: " + pathname + "\n\tFileID: " + fileId + "\n\tDesirable Replication Degree: " + desRepDegree + "\n\tChunks Info:\n" + chunksInfo;
        }

        if(Peer.getDataContainer().getOwnFiles().entrySet().size() == 0) {
            stateInfo += "\tNone on File System...\n";
        }

        stateInfo += "Stored Chunks:\n";

        //for each chunk stored
        for (HashMap.Entry<String, ChunkInfo> entry : Peer.getDataContainer().getBackedUpChunks().entrySet()) {
            ChunkInfo chunkInfo = entry.getValue();
            String chunkId = Chunk.buildChunkFileId(chunkInfo.getChunkNo());
            String fileId = chunkInfo.getFileId();
            File chunkFile = new File(Chunk.getPathname() + fileId + "/" + chunkId);
            //id
            String id = fileId + " -> " + chunkId;
            //size in KBytes
            long chunkFileLength = chunkFile.length();
            chunkFileLength /= 1000; //TODO ??? is this correct ???
            //currRepDegree
            int currRepDegree = chunkInfo.getCurrRepDegree();
            stateInfo+= "\tID: " + id + "\n\tSize(KBytes): " + chunkFileLength + "\n\tPerceived Replication Degree: " + currRepDegree + "\n";
        }

        if(Peer.getDataContainer().getBackedUpChunks().entrySet().size() == 0) {
            stateInfo += "\tNone on File System...\n";
        }

        //storage capacity
        long max_capacity = Peer.getDataContainer().getStorageCapacity();
        max_capacity /= 1000;
        File dir = new File(Chunk.getPathname());
        long used_capacity = Peer.getDataContainer().getCurrStorageAmount();
        used_capacity /= 1000;
        stateInfo += "Max Storage Capacity(KBytes): " + max_capacity + "\tUsed Storage Capacity(KBytes): " + used_capacity + "\n";

        System.out.println(stateInfo);
    }
}
