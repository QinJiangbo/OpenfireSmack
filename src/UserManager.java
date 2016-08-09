import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Richard on 7/9/15.
 */
public class UserManager {

    XMPPManager xmppManager = new XMPPManager();
    XMPPConnection connection = xmppManager.getConnection();

    /**
     * 登录到聊天服务器
     * @param userName 用户名
     * @param password 密码
     * @return 成功返回true, 失败返回false
     */
    public XMPPConnection loginServer(String userName, String password) {
        if(connection == null) {
            connection = xmppManager.getConnection();
        }
        try {
            connection.connect();
            connection.login(userName, password);
            return connection;
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 注册到服务器
     * @param userName 用户名
     * @param password 密码
     * @return "1": 注册成功
     *         "0": 服务器无响应
     *         "2": 用户名已存在
     *         "3": 注册失败
     */
    public String registerUser(String userName, String password) {
        connection = xmppManager.getConnection();
        try {
            connection.connect();
            if (connection == null) {
                return "0";
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        Registration registration = new Registration();
        registration.setType(IQ.Type.SET);
        registration.setTo(connection.getServiceName());
        //在这儿，用户名不是jid，而是jid的@符号前面一部分
        registration.setUsername(userName);
        registration.setPassword(password);
        //这个添加属性的参数不能为空，否则我们什么也得不到
        registration.addAttribute("android","geolo_createUser_android");
        registration.addAttribute("name",userName);

        PacketFilter filter = new AndFilter(new PacketIDFilter(registration.getPacketID()),new PacketTypeFilter(IQ.class));
        PacketCollector collector = connection.createPacketCollector(filter);
        connection.sendPacket(registration);

        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        // 停止查找结果
        collector.cancel();
        if (result == null) {
            System.out.println("No response from server.");
            return Constants.NO_RESULTS;
        } else if (result.getType() == IQ.Type.RESULT) {
            return Constants.REGISTER_SUCCESS;
        } else { // if (result.getType() == IQ.Type.ERROR)
            if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
                System.out.println("IQ.Type.ERROR: "
                        + result.getError().toString());
                return Constants.ACCOUNT_EXIST;
            } else {
                System.out.println("IQ.Type.ERROR: "
                        + result.getError().toString());
                return Constants.REGISTER_FAIL;
            }
        }
    }

    /**
     * 更改用户的密码
     * @param password 密码
     * @return 成功返回true，失败返回false
     */
    public boolean changePassword(XMPPConnection xmppConnection, String password) {
        try {
            connection = xmppConnection;
            if(!connection.isConnected()){
                connection.connect();
            }
            connection.getAccountManager().changePassword(password);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从聊天服务器上删除用户账号
     * @param xmppConnection
     * @return 成功返回true，失败返回false
     */
    public boolean deleteAccount(XMPPConnection xmppConnection) {
        try {
            connection = xmppConnection;
            if(!connection.isConnected()){
                connection.connect();
            }
            connection.getAccountManager().deleteAccount();
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 登出这个服务器
     * @return 成功返回true，失败返回false
     */
    public boolean logoutServer() {
        if(connection.isConnected()) {
            connection.disconnect();
            return true;
        }
        return false;
    }

    /**
     * 更改用户的在线状态
     * @param xmppConnection xmpp连接
     * @param code 状态类型
     * 0: 在线, 1: 离开, 2: 离线
     */
    public void changeUserStatus(XMPPConnection xmppConnection, int code) {
        Presence presence;
        switch (code) {
            case 0:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.available);
                xmppConnection.sendPacket(presence);
                break;
            case 1:
                presence = new Presence(Presence.Type.available);
                presence.setMode(Presence.Mode.away);
                xmppConnection.sendPacket(presence);
                break;
            case 2:
                presence = new Presence(Presence.Type.unavailable);
                xmppConnection.sendPacket(presence);
                break;
            default:
                break;
        }
    }

    /**
     * 判断OpenFire用户的状态 strUrl :
     * url格式 - http://SERVER_HOST:9090/plugins/presence
     * /status?jid=user1@SERVER_NAME&type=xml
     * 返回值 : 0 - 用户不存在; 1 - 用户在线; 2 - 用户离线
     * 说明 ：必须要求 OpenFire加载 presence 插件，同时设置任何人都可以访问
     */
    public int IsUserOnLine(String userName) {
        String url = "http://"+Constants.SERVER_HOST+":9090/plugins/presence/status?" +
                "jid="+ userName +"@"+ Constants.SERVER_NAME +"&type=xml";
        int shOnLineState = 0; // 不存在
        try {
            URL oUrl = new URL(url);
            URLConnection oConn = oUrl.openConnection();
            if (oConn != null) {
                BufferedReader oIn = new BufferedReader(new InputStreamReader(
                        oConn.getInputStream()));
                if (null != oIn) {
                    String strFlag = oIn.readLine();
                    oIn.close();
                    System.out.println("strFlag\n"+strFlag);
                    if (strFlag.indexOf("type=\"unavailable\"") >= 0) {
                        shOnLineState = 2;
                    }
                    if (strFlag.indexOf("type=\"error\"") >= 0) {
                        shOnLineState = 0;
                    } else if (strFlag.indexOf("priority") >= 0
                            || strFlag.indexOf("id=\"") >= 0) {
                        shOnLineState = 1;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shOnLineState;
    }
}
