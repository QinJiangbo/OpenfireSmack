import org.jivesoftware.smack.XMPPConnection;

import java.util.Scanner;

/**
 * Created by Richard on 7/20/15.
 */
public class TestFileManager {

    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        FileManager fileManager = new FileManager();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username:");
        String name = scanner.nextLine();
        System.out.println("Enter your password:");
        String password = scanner.nextLine();
        XMPPConnection conn = userManager.loginServer(name, password);
        userManager.changeUserStatus(conn, 0);
        System.out.println("Send or receive file? 1:send;2:receive");
        String mode = scanner.nextLine();
        if(mode.equals("1")) {
            System.out.println("Enter the receiver:");
            String receiver = scanner.nextLine();
            fileManager.sendFile(conn, receiver);
        }
        else if(mode.equals("2")) {
            fileManager.receiveFile(conn);
        }
    }
}
