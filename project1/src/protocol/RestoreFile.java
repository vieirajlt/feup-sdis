package protocol;


import protocol.subprotocol.FileManagement.FileManager;

import java.io.*;
import java.util.ArrayList;

public class RestoreFile extends FileManager implements Runnable {

    public RestoreFile(String fileId)  {
        setFileId(fileId);
        setChunks(Peer.getDataContainer().getTmpChunksChunks(getFileId()));
    }

    @Override
    public void run() {
        File file = new File("TMP/RESTORED/" + Peer.getServerId() + "/" + "restored.png");
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
