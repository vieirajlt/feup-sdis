package protocol;


import java.io.*;
import java.util.ArrayList;

public class RestoreFile implements Runnable {


    private  String fileId;
    private  ArrayList<Chunk> chunks;
    private int chunksSize;

    public RestoreFile(String fileId)  {
        this.fileId = fileId;
        this.chunks = new ArrayList<>();
        this.chunksSize = Peer.getDataContainer().getNrOfChunks(fileId);
    }


    private void loadChunks() {
        for(int chunkNo = 0; chunkNo < chunksSize; chunkNo++)
        {
            Chunk chunk = new Chunk(chunkNo);
            chunks.add(chunk.load(fileId,chunkNo));
        }
    }

    //TODO not working as needed
    @Override
    public void run() {
        loadChunks();
        File file = new File("TMP/Data/" + Peer.getServerId() + "/" + "restored.png");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file, true);

            for(int chunkNo = 0; chunkNo < chunksSize ; chunkNo++)
            {
               Chunk chunk = chunks.get(chunkNo);
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
