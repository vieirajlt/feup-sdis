package protocol.subprotocol;

import protocol.Chunk;
import protocol.Peer;
import protocol.subprotocol.FileManagement.RestoreFile;
import protocol.subprotocol.handler.ChunkHandler;
import protocol.subprotocol.handler.Handler;
import protocol.subprotocol.handler.PutchunkHandler;
import protocol.subprotocol.handler.StoredHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Receiver extends Subprotocol {

    private final static String LOG_PATH_1 = "TMP/LOGS/";
    private final static String LOG_PATH_2 = "/replication.log";

    private static String pathname;

    public Receiver() {
        pathname = LOG_PATH_1 + Peer.getServerId() + LOG_PATH_2;
        File file = new File(pathname);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean run(byte[] byteMessage) {

        ArrayList<byte[]> separator = headerBodySeparator(byteMessage);

        String header = new String(separator.get(0), StandardCharsets.UTF_8);
        byte[] body = separator.get(1);

        String[] cmd = header.split(" ", 2);

        if (cmd[0].equals(PUTCHUNK)) {
            putchunk(header, body);
        } else if (cmd[0].equals(GETCHUNK)) {
            getchunk(header);
        } else if (cmd[0].equals(REMOVED)) {
            removed(header);
        } else if (cmd[0].equals(DELETE)) {
            delete(header);
        } else if (cmd[0].equals(STORED)) {
            stored(header);
        } else if (cmd[0].equals(CHUNK)) {
            chunk(header, body);
        } else {
            return false;
        }
        return true;
    }

    private void putchunk(String headerStr, byte[] body) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.putchunk");
            String[] header = headerStr.split(" ");

            if (!checkHeader(header))
                return;


            int chunkNo = Integer.parseInt(header[4]);
            Chunk chunk = new Chunk(chunkNo, body);

            String fileId = header[3];

            //case it is Peer's own file
            if(Peer.getDataContainer().getNrOfChunks(fileId) !=  null)
                return;

            int repDegree = Integer.parseInt(header[5]);

            StoredHandler storedHandler = new StoredHandler(fileId, chunk, repDegree);
            new Thread(storedHandler).start();
        }
    }

    private void getchunk(String headerStr) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.getchunk");
            String[] header = headerStr.split(" ");
            String senderId = header[2];

            int chunkNo = Integer.parseInt(header[4]);
            String fileId = header[3];

            ChunkHandler chunkHandler = new ChunkHandler(fileId, chunkNo); //CHUNK
            new Thread(chunkHandler).start();
        }
    }

    private void delete(String headerStr) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.delete");
            String[] header = headerStr.split(" ");
            String fileId = header[3];

            if (!checkHeader(header))
                return;

            File folder = new File(Chunk.STORE_PATH + Peer.getServerId());
            File[] listOfFiles = folder.listFiles();

            String storedFileId, chunkId;

            long length;

            //delete all the chunks from the file
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    chunkId = listOfFiles[i].getName();
                    storedFileId = chunkId.split("_")[0];

                    if(storedFileId.equals(fileId)) {

                        length = listOfFiles[i].length();
                        //dec current storage amount

                        //delete file
                       if(!listOfFiles[i].delete())
                       {
                           System.out.println("Failed to delete the file");
                           return;
                       }

                       //decrement current storage amount
                       Peer.getDataContainer().decCurrStorageAmount(length);

                       //delete file from backedUpChunks map
                        Peer.getDataContainer().deleteBackedUpChunk(chunkId);
                    }
                }
            }

            //delete all the file chunks from peersChunks
            Peer.getDataContainer().deletePeersFileChunks(fileId);

        }
    }

    private void removed(String headerStr) {
        synchronized (this) {
            System.out.println("protocol.subprotocol.Receiver.removed");
            String[] header = headerStr.split(" ");

            String fileId = header[3];
            int chunkNo = Integer.parseInt(header[4]);

            String chunkId = Chunk.buildChunkId(fileId,chunkNo);
            Peer.getDataContainer().decBackedUpChunkCurrRepDegree(chunkId);

            //if the peer does not store the chunk
            if(!Peer.getDataContainer().hasBackedUpChunk(chunkId))
                return;

            //random delay uniformly distributed between 0 and 400 ms
            try {
                Thread.sleep(Handler.buildSleep_time_ms());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //if no putchunk message was received send one
            if(Peer.getDataContainer().getDifferenceBtCurrDesiredRepDegrees(chunkId) < 0) {

              Chunk chunk = new Chunk(chunkNo);
              int replicationDegree = Peer.getDataContainer().getBackedUpChunkDesiredRepDegree(chunkId);

              PutchunkHandler putchunkHandler = new PutchunkHandler(chunk.load(fileId, chunkNo),fileId,replicationDegree);
              new Thread(putchunkHandler).start();
            }
        }
    }

    private void stored(String headerStr) {
        synchronized (this) {
            String[] header = headerStr.split(" ");
            String senderId = header[2];
            String fileId = header[3];
            int chunkNo = Integer.parseInt(header[4]);

            if (!checkHeader(header))
                return;
            System.out.println("protocol.subprotocol.Receiver.stored");

            String chunkId = fileId + "_" + chunkNo;
            Peer.getDataContainer().incStoredCurrRepDegree(chunkId);
            Peer.getDataContainer().incBackedUpChunkCurrRepDegree(chunkId);
        }
    }

    private void chunk(String headerStr, byte[] body) {
        synchronized (this) {
            String[] header = headerStr.split(" ");

            if (!checkHeader(header))
                return;
            System.out.println("protocol.subprotocol.Receiver.chunk");

            String senderId = header[2];
            String fileId = header[3];
            int chunkNo = Integer.parseInt(header[4]);

            ArrayList<Chunk> chunks = Peer.getDataContainer().getTmpChunksChunks(fileId);

            //if not initialized, start it full of nulls with the required size
            if (chunks == null) {
                if(Peer.getDataContainer().getNrOfChunks(fileId) == null)
                    return;
                int chunksSize = Peer.getDataContainer().getNrOfChunks(fileId);
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
                    new Thread(restoreFile).start();
                }
            }
        }
    }

    private boolean checkHeader(String[] header) {
        return Float.parseFloat(header[1]) == Peer.getProtocolVersion()
                && Integer.parseInt(header[2]) != Peer.getServerId();
    }

    private ArrayList<byte[]> headerBodySeparator(byte[] byteMessage) {
        ArrayList<byte[]> separator = new ArrayList<>();

        for (int index = 0; index < byteMessage.length; ++index) {
            if (byteMessage[index] == Handler.CR &&
                    byteMessage[index + 1] == Handler.LF &&
                    byteMessage[index + 2] == Handler.CR &&
                    byteMessage[index + 3] == Handler.LF) {
                index += 4;
                byte[] header = Arrays.copyOfRange(byteMessage, 0, index);
                byte[] body = Arrays.copyOfRange(byteMessage, index, byteMessage.length);
                separator.add(header);
                separator.add(body);
                return separator;
            }
        }
        return null;
    }

}
