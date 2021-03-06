package protocol.subprotocol.handler;

import protocol.Chunk;
import protocol.Peer;
import protocol.info.ChunkInfo;
import protocol.info.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
            Path path = Paths.get(entry.getValue().getPath());
            String pathname = path.toAbsolutePath().toString();
            //backup service fileId
            String fileId = entry.getKey();
            //desirable repDegree
            int desRepDegree = entry.getValue().getRepDegree();
            //for each chunk, id and currRepDegree
            String chunksInfo = "";
            for (int chunkNo = 0; chunkNo < entry.getValue().getNrOfChunks(); ++chunkNo) {
                Chunk chunk = new Chunk(chunkNo);
                String chunkId = chunk.buildChunkId();
                String chunkKey = chunk.buildChunkKey(fileId);
                int currRepDegree = Peer.getDataContainer().getStoredCurrRepDegree(chunkKey);
                chunksInfo += "\t\tID: " + chunkId + "\tPerceived Replication Degree: " + currRepDegree + "\n";
            }
            stateInfo += "\tPathname: " + pathname + "\n\tFileID: " + fileId + "\n\tDesirable Replication Degree: " + desRepDegree + "\n\tChunks Info:\n" + chunksInfo;
        }

        if(Peer.getDataContainer().getOwnFiles().entrySet().size() == 0) {
            stateInfo += "\tNone on File System...\n";
        }

        stateInfo += "Stored Chunks:\n";

        ArrayList<String> stored = new ArrayList<>();
        //for each chunk stored
        boolean hasChunks = false;
        for (HashMap.Entry<String, ChunkInfo> entry : Peer.getDataContainer().getBackedUpChunks().entrySet()) {
            ChunkInfo chunkInfo = entry.getValue();
            if(!chunkInfo.isOnPeer())
                continue;
            hasChunks = true;
            Chunk chunk = new Chunk(chunkInfo.getChunkNo());
            String chunkId = chunk.buildChunkId();
            String fileId = chunkInfo.getFileId();
            Path path = Paths.get(Chunk.getChunkFolderPath() + fileId + "/" + chunkId);
            //id
            String id = fileId + " -> " + chunkId;
            //size in KBytes
            int chunkFileLength = chunkInfo.getSize();
            chunkFileLength /= 1000;
            //currRepDegree
            int currRepDegree = chunkInfo.getCurrRepDegree();
            String chunkInfoStr = "\tID: " + id + "\n\tSize(KBytes): " + chunkFileLength + "\n\tPerceived Replication Degree: " + currRepDegree + "\n";
            stored.add(chunkInfoStr);
        }
        Collections.sort(stored);
        for(String info : stored) {
            stateInfo += info;
        }

        if(!hasChunks) {
            stateInfo += "\tNone on File System...\n";
        }

        //storage capacity
        long max_capacity = Peer.getDataContainer().getStorageCapacity();
        max_capacity /= 1000;
        long used_capacity = Peer.getDataContainer().getCurrStorageAmount();
        used_capacity /= 1000;
        stateInfo += "Max Storage Capacity(KBytes): " + max_capacity + "\tUsed Storage Capacity(KBytes): " + used_capacity + "\n";

        System.out.println(stateInfo);
    }
}
