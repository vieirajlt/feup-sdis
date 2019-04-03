package protocol.subprotocol.handler;

import protocol.Peer;
import protocol.subprotocol.FileManagement.SplitFile;

import static protocol.subprotocol.Subprotocol.DELETE;

public class DeleteHandler extends Handler implements Runnable{

    private static final int MAX_DELETE_REPEAT = 5;

    private static final int DELETE_INBETWEEN_TIME_MS = 1000;

    private int repeatCnt = 0;

    private SplitFile sf;

    public DeleteHandler(SplitFile sf) {
        this.sf = sf;
    }

    @Override
    public void run() {

        byte[] message = buildMessage(DELETE, MSG_CONFIG_DELETE, sf.getFileId(), -1, -1, null);

        while (repeatCnt < MAX_DELETE_REPEAT )
        {
            Peer.getControlChannel().write(message);

            ++repeatCnt;

            //delay
            try {
                Thread.sleep(DELETE_INBETWEEN_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //delete the file
        if(sf.getFile().delete())
            System.out.println("File deleted successfully");
        else
        {
            System.out.println("Failed to delete the file");
            return;
        }

        //delete file from Peer's ownFiles and file chunks from stored
        Peer.getDataContainer().deleteOwnFileAndChunks(sf.getFileId());
    }
}
