package protocol.subprotocol.fileManagement;


import protocol.Chunk;
import protocol.Peer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class RestoreFile extends FileManager {

    public RestoreFile(String fileId) {
        setFileId(fileId);
        setChunks(Peer.getDataContainer().getTmpChunksChunks(getFileId()));
    }

    public RestoreFile( List<Chunk> chunks) {
        setChunks(chunks);
    }

    public void process(String clientID, String fileName) {

        System.out.println("restorefile process");

        Path path = Paths.get("TMP/client" + clientID + "/restored/" + fileName);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            for (Chunk chunk : getChunks()) {
                stream.write(chunk.getBody());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] fileArr = stream.toByteArray();

        try {
            Files.createDirectories(path.getParent());
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            ByteBuffer buffer = ByteBuffer.wrap(fileArr);

            fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    System.out.println("Success restoring file");
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.err.println("Error restoring chunk file");
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
