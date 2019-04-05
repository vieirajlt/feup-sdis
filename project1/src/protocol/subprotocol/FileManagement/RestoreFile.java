package protocol.subprotocol.FileManagement;


import protocol.Chunk;
import protocol.Peer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RestoreFile extends FileManager{

    public RestoreFile(String fileId)  {
        setFileId(fileId);
        setChunks(Peer.getDataContainer().getTmpChunksChunks(getFileId()));
    }

    public void process() {
        String fileName = Peer.getDataContainer().getOwnFileName(getFileId());
        File file = new File("TMP/" + Peer.getServerId() + "/restored/" + fileName);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file, true);

            for(Chunk chunk : getChunks()) {
                fOut.write(chunk.getBody());
            }

            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
     
    }

}
