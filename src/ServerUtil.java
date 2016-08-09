import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smackx.provider.*;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;

import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.*;

/**
 * Created by Richard on 7/5/15.
 */
public class ServerUtil {
    private static XMPPConnection connection = null;

    /**
     * connection configuration
     */
    public static String SERVER_HOST = "127.0.0.1";
    public static int PORT = 5222;
    public static String SERVER_NAME = "127.0.0.1";

    private Map<String, Chat> chatManage = new HashMap<String, Chat>();

    /**
     * get the xmpp connection
     * @return xmpp connection
     */
    public XMPPConnection getConnection() {
        XMPPConnection.DEBUG_ENABLED = false;

        /**
         * three parameters and two parameters
         */
        final ConnectionConfiguration configuration = new ConnectionConfiguration(SERVER_HOST, PORT);

        // allow automatically connection
        configuration.setReconnectionAllowed(true);
        configuration.setSASLAuthenticationEnabled(false);
        configuration.setSendPresence(false);

        if(connection == null) {
            connection = new XMPPConnection(configuration);
        }
        // 配置各种Provider，如果不配置，则会无法解析数据
        configureConnection(ProviderManager.getInstance());

        return connection;
    }

    /**
     * 加入providers的函数 ASmack在/META-INF缺少一个smack.providers 文件
     *
     * @param pm
     */
    public void configureConnection(ProviderManager pm) {

        // Private Data Storage
        pm.addIQProvider("query", "jabber:iq:private",
                new PrivateDataManager.PrivateDataIQProvider());

        // Time
        try {
            pm.addIQProvider("query", "jabber:iq:time",
                    Class.forName("org.jivesoftware.smackx.packet.Time"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Roster Exchange
        pm.addExtensionProvider("x", "jabber:x:roster",
                new RosterExchangeProvider());

        // Message Events
        pm.addExtensionProvider("x", "jabber:x:event",
                new MessageEventProvider());

        // Chat State
        pm.addExtensionProvider("active",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("composing",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("paused",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("inactive",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());
        pm.addExtensionProvider("gone",
                "http://jabber.org/protocol/chatstates",
                new ChatStateExtension.Provider());

        // XHTML
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
                new XHTMLExtensionProvider());

        // Group Chat Invitations
        pm.addExtensionProvider("x", "jabber:x:conference",
                new GroupChatInvitation.Provider());

        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
                new DiscoverItemsProvider());

        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());

        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

        // MUC User
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
                new MUCUserProvider());

        // MUC Admin
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
                new MUCAdminProvider());

        // MUC Owner
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
                new MUCOwnerProvider());

        // Delayed Delivery
        pm.addExtensionProvider("x", "jabber:x:delay",
                new DelayInformationProvider());

        // Version
        try {
            pm.addIQProvider("query", "jabber:iq:version",
                    Class.forName("org.jivesoftware.smackx.packet.Version"));
        } catch (ClassNotFoundException e) {
            // Not sure what's happening here.
        }

        // VCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
                new OfflineMessageRequest.Provider());

        // Offline Message Indicator
        pm.addExtensionProvider("offline",
                "http://jabber.org/protocol/offline",
                new OfflineMessageInfo.Provider());

        // Last Activity
        pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

        // User Search
        pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

        // SharedGroupsInfo
        pm.addIQProvider("sharedgroup",
                "http://www.jivesoftware.org/protocol/sharedgroup",
                new SharedGroupsInfo.Provider());

        // JEP-33: Extended Stanza Addressing
        pm.addExtensionProvider("addresses",
                "http://jabber.org/protocol/address",
                new MultipleAddressesProvider());

        // FileTransfer
        pm.addIQProvider("si", "http://jabber.org/protocol/si",
                new StreamInitiationProvider());

        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
                new BytestreamsProvider());

        // Privacy
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        pm.addIQProvider("command", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider());
        pm.addExtensionProvider("malformed-action",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.MalformedActionError());
        pm.addExtensionProvider("bad-locale",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadLocaleError());
        pm.addExtensionProvider("bad-payload",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadPayloadError());
        pm.addExtensionProvider("bad-sessionid",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.BadSessionIDError());
        pm.addExtensionProvider("session-expired",
                "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider.SessionExpiredError());
    }

    /**
     * login to the server
     * @param userName username
     * @param password password
     * @return true successful, false failed
     */
    public boolean loginServer(String userName, String password) {
        if(connection == null) {
            getConnection();
        }
        try {
            connection.connect();
            connection.login(userName, password);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * register user to the server
     * @param userName username
     * @param password password
     * @return "1": register successfully
     * "0": no results from server
     * "2": username already exists
     * "3": register failed
     */
    public String registerUser(String userName, String password) {
        getConnection();
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
        // here, the username is not the jid, but part of it front of "@" symbol
        registration.setUsername(userName);
        registration.setPassword(password);
        // addAttribute can not be empty, or we will get nothing
        registration.addAttribute("android","geolo_createUser_android");
        registration.addAttribute("name",userName);

        PacketFilter filter = new AndFilter(new PacketIDFilter(registration.getPacketID()),new PacketTypeFilter(IQ.class));
        PacketCollector collector = connection.createPacketCollector(filter);
        connection.sendPacket(registration);

        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
        // stop querying results
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
     * change the password of the user
     * @param password password
     * @return true or false
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
     * delete the user account from the server
     * @param xmppConnection
     * @return true or false
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
     * logout the server
     * @return true or false
     */
    public boolean logoutServer() {
        if(connection.isConnected()) {
            connection.disconnect();
            return true;
        }
        return false;
    }

    /**
     * change the status and mood of user
     * @param xmppConnection connection
     * @param code status type
     * 0: online, 1: away, 2: offline
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
        String url = "http://"+SERVER_HOST+":9090/plugins/presence/status?" +
                "jid="+ userName +"@"+ SERVER_NAME +"&type=xml";
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

    /**
     * add private group
     * @param groupName
     * @return true if success, false if fails
     */
    public boolean addGroup(String groupName) {
        if(getConnection() == null) {
            return false;
        }
        getConnection().getRoster().createGroup(groupName);
        String owner = getConnection().getUser();
        String name = getConnection().getAccountManager().getAccountAttribute("name");
        try {
            getConnection().getRoster().createEntry(owner, name, new String[]{groupName});
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * get all groups in the databases
     * @param xmppConnection
     * @return
     */
    public List<RosterGroup> getGroups(XMPPConnection xmppConnection) {
        if (!xmppConnection.isConnected()) {
            try {
                xmppConnection.connect();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
        List<RosterGroup> list = new ArrayList<RosterGroup>();
        String user = xmppConnection.getUser().split("/")[0];
        System.out.println(user);
        Collection<RosterGroup> rosterGroup = xmppConnection.getRoster().getEntry(user).getGroups();
        Iterator<RosterGroup> it = rosterGroup.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    /**
     * get all the users in one group
     * @param xmppConnection
     * @param groupName
     * @return
     */
    public List<RosterEntry> getAllUsersInGroup(XMPPConnection xmppConnection, String groupName) {
        if (!xmppConnection.isConnected()) {
            try {
                xmppConnection.connect();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
        List<RosterEntry> list = new ArrayList<RosterEntry>();
        RosterGroup rosterGroup = xmppConnection.getRoster().getGroup(groupName);
        Collection<RosterEntry> rosterEntries = rosterGroup.getEntries();
        Iterator<RosterEntry> it = rosterEntries.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    /**
     * remove the group by group name
     * @param roster
     * @param groupName
     * @return
     */
    public boolean removeGroup(Roster roster, String groupName) {
        return true;
    }

    /**
     * add friend without group
     * @param userName friend name
     * @param name alias
     * @return true or false
     */
    public boolean addFriend(String userName, String name) {
        if (getConnection() == null) {
            return false;
        }
        try {
            getConnection().getRoster().createEntry(userName, name, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * add friend with group
     * @param userName
     * @param name
     * @param groupName
     * @return true or false
     */
    public boolean addFriend(String userName, String name, String groupName) {
        if (getConnection() == null) {
            return false;
        }
        try {
            Presence presence = new Presence(Presence.Type.subscribed);
            presence.setTo(userName);
            userName += "@"+getConnection().getServiceName();
            getConnection().sendPacket(presence);
            getConnection().getRoster().createEntry(userName, name, new String[]{groupName});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * remove friend from the list
     * @param userName
     * @return true or false
     */
    public boolean removeFriend(String userName) {
        if (getConnection() == null) {
            return false;
        }
        try {
            RosterEntry entry = null;
            if (userName.contains("@")) {
                entry = getConnection().getRoster().getEntry(userName);
            }
            else {
                entry = getConnection().getRoster().getEntry(
                        userName + "@" + getConnection().getServiceName());
            }
            if (entry == null) {
                entry = getConnection().getRoster().getEntry(userName);
            }
            getConnection().getRoster().removeEntry(entry);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * search the user by their usernames
     * @param userName
     * @return the list of users
     */
    public List<HashMap<String, String>> searchUser(String userName) {
        if (getConnection() == null) {
            return null;
        }
        HashMap<String, String> user = null;
        List<HashMap<String,String>> results = new ArrayList<HashMap<String, String>>();
        try {
            //discover service
            new ServiceDiscoveryManager(getConnection());
            UserSearchManager userSearchManager = new UserSearchManager(getConnection());
            Form searchForm = userSearchManager.getSearchForm(getConnection().getServiceName());
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("userAccount", true);
            answerForm.setAnswer("userPhote", userName);
            ReportedData data = userSearchManager.getSearchResults(answerForm,"search"+getConnection().getServiceName());
            Iterator<Row> it = data.getRows();
            Row row = null;
            while (it.hasNext()) {
                user = new HashMap<String, String>();
                row = it.next();
                user.put("userAccount", row.getValues("userAccount").next()
                        .toString());
                user.put("userPhote", row.getValues("userPhote").next()
                        .toString());
                results.add(user);
                // 若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * get the chat with friend
     * @param friend
     * @param listener
     * @return
     */
    public Chat getFriendChat(String friend, MessageListener listener) {
        if (getConnection() == null) {
            return null;
        }
        for (String friendStr: chatManage.keySet()) {
            if (friendStr.equals(friend)) {
                return chatManage.get(friendStr);
            }
        }
        /**
         * create chat window
         */
        Chat chat = getConnection().getChatManager().createChat(friend + "@" +getConnection().getServiceName(), listener);
        chatManage.put(friend, chat);
        return chat;
    }

    /**
     * send message to friend
     * @param friend friend username
     * @param message text message
     */
    public void sendMessage(String friend, String message) {
        Chat chat = getFriendChat(friend, null);
        try {
            //String messageJson = "{\"messageType\":\""+messageType+"\",\"chanId\":\""+chanId+"\",\"chanName\":\""+chanName+"\"}";
            chat.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * add chat listener to the chat manager
     */
    public void addListener() {
        getConnection().getChatManager().addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean b) {
                chat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        StringUtils.parseName(getConnection().getUser());
                        String from = message.getFrom();
                        String content = message.getBody();
                        System.out.println("From " + from + " : "+ content);
                    }
                });
            }
        });
    }

    /**
     * fetch the offline messages from the server
     */
    public void getOfflineMessages() {
        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(getConnection());
        try {
            Iterator<Message> it = offlineMessageManager.getMessages();
            System.out.println("Offline message number: " + offlineMessageManager.getMessageCount());
            Map<String, ArrayList<Message>> offlineMessages = new HashMap<String, ArrayList<Message>>();
            while (it.hasNext()) {
                Message message = it.next();
                System.out.println("Message received from " + message.getFrom().split("/")[0].split("@")[0]
                        + " - message: " + message.getBody() );
                String fromUser = message.getFrom().split("/")[0];

                if (offlineMessages.containsKey(fromUser)) {
                    offlineMessages.get(fromUser).add(message);
                }
                else {
                    ArrayList<Message> temp = new ArrayList<Message>();
                    temp.add(message);
                    offlineMessages.put(fromUser, temp);
                }
            }

            Set<String> keys = offlineMessages.keySet();
            Iterator<String> offIt = keys.iterator();
            while (offIt.hasNext()) {
                String key = offIt.next();
                ArrayList<Message> messages = offlineMessages.get(key);
                for (int i = 0; i < messages.size(); i++ ) {
                    //System.out.println(messages.get(i).getType());
                }
            }
            offlineMessageManager.deleteMessages();

        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * test functions
     * @param args
     */
    public static void main(String[] args) {
        ServerUtil util = new ServerUtil();
        util.loginServer("Richard","cq08010907");
//        util.changeUserStatus(util.getConnection(),0);
//        int state = util.IsUserOnLine("Richard");
//        String username = util.getConnection().getAccountManager().getAccountAttribute("username");
//        String password = util.getConnection().getAccountManager().getAccountAttribute("password");
//        String result = util.registerUser("Jeff","cq08010907");
//        boolean result2 = util.changePassword(util.getConnection(),"qjb940907");
//        boolean result3 = util.logoutServer();
//        System.out.println(result);
//        System.out.println("Enter you name:");
//        Scanner scanner = new Scanner(System.in);
//        String userName = scanner.nextLine();
//        System.out.println("Enter you password");
//        String password = scanner.nextLine();
//        util.loginServer(userName,password);
//        util.changeUserStatus(util.getConnection(),0);
//        System.out.println("Enter the friend name:");
//        String friend = scanner.nextLine();
//        util.addListener();
//        while (true) {
//            String body = scanner.nextLine();
//            util.sendMessage(friend, body);
//        }

//        util.loginServer("Richard", "cq08010907");
//        util.getOfflineMessages();
//        util.changeUserStatus(util.getConnection(), 0);
//        System.out.println(util.addGroup("WHU"));
//        System.out.println(util.getConnection().getUser());
        System.out.println(util.getGroups(util.getConnection()).get(0).getName());
//        int i = util.getConnection().getRoster().getGroups().size();
//        System.out.println(i);
//        System.out.println(util.getAllUsersInGroup(util.getConnection(), "CCNU"));
//        System.out.println(util.addFriend("Richard", "Friend", "WHU"));
    }


}
