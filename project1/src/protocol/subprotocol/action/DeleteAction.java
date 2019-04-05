package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;

import java.io.File;

public class DeleteAction extends Action {

    private String fileId;

    public DeleteAction(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public void process() {
        System.out.println(Chunk.getChunkFolderPath(fileId));
        File folder = new File(Chunk.getChunkFolderPath(fileId));
        if (!folder.exists())
            return;
        File[] listOfFiles = folder.listFiles();

        //delete all the chunks from the file
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String chunkNoStr = ((file.getName()).substring(3)).split("\\.")[0];
                int chunkNo = Integer.parseInt(chunkNoStr);
                String key = Chunk.buildChunkKey(fileId, chunkNo);
                //dec current storage amount

                //delete file
                if (!file.delete()) {
                    System.out.println("Failed to delete the file");
                    return;
                }

                //delete file from backedUpChunks map
                Peer.getDataContainer().deleteBackedUpChunk(key);
            }
        }

        if (folder.list().length == 0) {
            folder.delete();
        }

        //delete all the file chunks from peersChunks
        Peer.getDataContainer().deletePeersFileChunks(fileId);
    }
}
