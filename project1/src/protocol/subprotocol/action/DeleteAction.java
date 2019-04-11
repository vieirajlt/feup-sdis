package protocol.subprotocol.action;

import protocol.Chunk;
import protocol.Peer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class DeleteAction extends Action {

    private String fileId;

    public DeleteAction(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public void process() {
        System.out.println(Chunk.getChunkFolderPath(fileId));
        Path dirPath = Paths.get(Chunk.getChunkFolderPath(fileId));
        if (!Files.exists(dirPath))
            return;
        try {
            Files.walk(dirPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File->{
                        if(File.isFile())
                            Peer.getDataContainer().decCurrStorageAmount(File.length());
                    File.delete();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //delete all the file chunks from peersChunks
        Peer.getDataContainer().deletePeersFileChunks(fileId);
        Peer.getDataContainer().deleteBackedUpFileChunks(fileId);
    }
}
