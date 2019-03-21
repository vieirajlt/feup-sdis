package protocol;


import java.io.*;

public class RestoreFile implements Runnable {


    private static String fileId;
    public RestoreFile(String id)  {
        fileId = id;
    }

    //TODO not working as needed
    @Override
    public void run() {
        File file = new File("TMP/Data/" + Peer.getServerId() + "/" + "restored.png");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            ObjectOutputStream oOut = new ObjectOutputStream(fOut);
            for(int chunkNo = 0; chunkNo < Peer.getDataContainer().getNrOfChunks(fileId) -1; chunkNo++)
            {
                oOut.write(Peer.getRestoredChunks().get(Chunk.buildChunkId(fileId,chunkNo)));
                System.out.println(chunkNo);
            }
          
            oOut.close();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
     
    }

}
