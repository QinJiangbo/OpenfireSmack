import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;

import java.util.*;

/**
 * Created by Richard on 7/9/15.
 */
public class TestSingleChat {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        XMPPManager xmppManager = new XMPPManager();
        SingleChatManager chattingManager = new SingleChatManager();

        System.out.println("Enter your name:");
        Scanner scanner = new Scanner(System.in);
        String userName = scanner.nextLine();
        System.out.println("Enter your password:");
        String password = scanner.nextLine();
        XMPPConnection xmppConnection = userManager.loginServer(userName, password);
        userManager.changeUserStatus(xmppManager.getConnection(),0);


//        List<RosterEntry> list = chattingManager.getAllUsersInGroup(xmppConnection, "WHU");
//        Iterator<RosterEntry> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next());
//        }


//        System.out.println("Enter the username you want to search:");
//        String searchName = scanner.nextLine();
//        List<HashMap<String, String>> list = chattingManager.searchUser(searchName);
//        for (HashMap<String, String> map: list) {
//            System.out.println(map.get("username").toString());
//            System.out.println(map.get("name").toString());
//            System.out.println(map.get("email").toString());
//        }


//        Collection<RosterGroup> list = chattingManager.getGroups(xmppConnection);
//        Iterator<RosterGroup> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next().getName());
//        }


//        chattingManager.addGroup("ceceSAT");


//        List<RosterEntry> list = chattingManager.getAllUsers(xmppConnection);
//        Iterator<RosterEntry> iterator = list.iterator();
//        while (iterator.hasNext()) {
//            System.out.println(iterator.next());
//        }

//        System.out.println("Enter the friend name:");
//        String friendName = scanner.nextLine();
//        chattingManager.addListener();
//        System.out.println("Now, Starts chatting...");
//        while (true) {
//            String message = scanner.nextLine();
//            chattingManager.sendMessage(friendName, message);
//        }


//        chattingManager.getOfflineMessages();
//        userManager.changeUserStatus(xmppManager.getConnection(),0);


//        chattingManager.changeNickName("amy","Love");


//        chattingManager.removeFriend("jeff");

//        chattingManager.addGroup("Teacher");
//        chattingManager.addFriend("amy", "Amy", "Teacher");
//        chattingManager.addFriend("maomao", "Student3", "Students");

//        chattingManager.moveFriendToGroup("maomao","WHU");

    }
}
