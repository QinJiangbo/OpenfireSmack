import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.*;
import org.jivesoftware.smackx.filetransfer.FileTransfer;

import java.io.File;

/**
 * Created by Richard on 7/10/15.
 */
public class FileManager {

    XMPPManager xmppManager = new XMPPManager();
    /**
     * 发送文件
     * @param receiveUser 接收方用户名
     */
    public void sendFile(XMPPConnection connection, String receiveUser) {
        String receiverJID = receiveUser + "@" + Constants.SERVER_HOST;
        Presence presence = connection.getRoster().getPresence(receiverJID);
        System.out.println("from "+presence.getFrom());
        System.out.println("to "+presence.getTo());
        if (presence.getType() != Presence.Type.unavailable) {
            System.out.println("Starts to send file...");
            FileTransferManager fileTransferManager = new FileTransferManager(connection);
            OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(presence.getFrom());
            try {
                transfer.sendFile(new File("/Users/Richard/Documents/ID.png"),"[description]ID Card");
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            while (!transfer.isDone()) {
                if (transfer.getStatus() == FileTransfer.Status.in_progress) {
                    System.out.println(transfer.getStatus());
                    System.out.println(transfer.getProgress());
                    System.out.println(transfer.isDone());
                }
            }
        }
        else {
            System.out.println("The receiver is not online!");
        }
    }

    /**
     * 接收文件
     */
    public void receiveFile(XMPPConnection connection) {
        System.out.println(connection.getUser());
        FileTransferManager fileTransferManager = new FileTransferManager(connection);
        System.out.println("Starts listening...");
        fileTransferManager.addFileTransferListener(new FileTransferListener() {
            @Override
            public void fileTransferRequest(FileTransferRequest fileTransferRequest) {

                final IncomingFileTransfer transfer = fileTransferRequest.accept();
                final String fileName = transfer.getFileName();
                long length = transfer.getFileSize();
                final String fromUser = fileTransferRequest.getRequestor().split("/")[0];
                System.out.println("File Size: " + length + "  " + fileTransferRequest.getRequestor());
                System.out.println("File Type: " + fileTransferRequest.getMimeType());
                try {
                    System.out.println("Receiving file: " + fileName);
                    transfer.recieveFile(new File("/Users/Richard/Downloads/RR.png"));
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * 下面的代码是为了保证接收方监听一直存在
         */
        while (true) {
            // do something here
        }
    }
}
