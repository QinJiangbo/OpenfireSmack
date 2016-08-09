import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.muc.*;

import java.util.Iterator;
import java.util.List;

import java.util.Scanner;

/**
 * Created by Richard on 7/9/15.
 */
public class TestMultiChat {

    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        XMPPManager xmppManager = new XMPPManager();
        MultiChatManager multiChatManager = new MultiChatManager();
        MultiUserChat muc = null;
        Scanner input = new Scanner(System.in);
        System.out.println("username:");
        String uname = input.nextLine();
        System.out.println("password:");
        String pwd = input.nextLine();
        XMPPConnection xmppConnection = userManager.loginServer(uname, pwd);
        userManager.changeUserStatus(xmppManager.getConnection(), 0);


//        System.out.println("way:");
//        String way = input.nextLine();
//        System.out.println("roomName:");
//        String roomName = input.nextLine();
//        System.out.println("access password:");
//        String password = input.nextLine();
//        if(way.equals("1")) {
//            muc = multiChatManager.joinMultiUserChat(uname, roomName, password);
//        }else if(way.equals("2")) {
//            muc = multiChatManager.createRoom(xmppConnection, roomName, password);
//        }
//        multiChatManager.addListener(muc);
//
//        System.out.println("Let's starts chatting!");
//        String message = "";
//        while (true) {
//            try {
//                message = input.nextLine();
//                if (message.equals("quit")) {
//                    userManager.logoutServer();
//                    break;
//                }
//                else{
//                    muc.sendMessage(message);
//                }
//            } catch (XMPPException e) {
//                e.printStackTrace();
//            }
//        }


//        System.out.println("Enter the room name:");
//        String roomName = input.nextLine();
//        System.out.println("Enter the nick name:");
//        String nickName = input.nextLine();
//        boolean flag = multiChatManager.registerMember(xmppConnection, roomName, nickName);
//        System.out.println(flag);


//        RoomInfo roomInfo = multiChatManager.searchRoom(xmppConnection, "honor");
//        System.out.println(roomInfo.isPasswordProtected());


//        System.out.println("Enter the room name:");
//        String roomName = input.nextLine();
//        System.out.println("Enter the new nick name:");
//        String nickName = input.nextLine();
//        System.out.println("Enter the access password:");
//        String password = input.nextLine();
//        boolean flag = multiChatManager.changeNickName(xmppConnection, uname, password, roomName, nickName);
//        System.out.println(flag);
//        while (true){
//
//        }


//        System.out.println("Enter the room name:");
//        String roomName = input.nextLine();
//        System.out.println("Enter the name of the admin:");
//        String adminUser = input.nextLine();
//        boolean flag = multiChatManager.addAdmin(xmppConnection,roomName,adminUser);
//        System.out.println(flag);


//        System.out.println("Enter the room name:");
//        String roomName = input.nextLine();
//        boolean flag = multiChatManager.destroyChatRoom(xmppConnection, roomName);
//        System.out.println(flag);


//        System.out.println("Enter the room name:");
//        String roomName = input.nextLine();
//        System.out.println("Enter the password:");
//        String password = input.nextLine();
//        List<Affiliate> list = multiChatManager.getMucMembers(xmppConnection, roomName, password);
//        for (Affiliate affiliate: list) {
//            System.out.println(affiliate.getJid());
//        }


//        Iterator<String> iterator = multiChatManager.getJoinedRooms(xmppConnection);
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next().toString());
//        }


//        List<HostedRoom> list = multiChatManager.getHostedRooms(xmppConnection);
//        Iterator<HostedRoom> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next().getJid());
//        }
    }
}
