import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;

import java.util.*;

/**
 * Created by Richard on 7/9/15.
 */
public class SingleChatManager {
    XMPPManager xmppManager = new XMPPManager();
    XMPPConnection connection = xmppManager.getConnection();

    private Map<String, Chat> chatManage = new HashMap<String, Chat>();

    /**
     * 添加好友分组
     * @param groupName 组名
     * @return 成功返回true, 失败返回false
     */
    public boolean addGroup(String groupName) {
        if(connection == null) {
            return false;
        }
        xmppManager.getConnection().getRoster().createGroup(groupName);
        String owner = xmppManager.getConnection().getUser();
        String name = xmppManager.getConnection().getAccountManager().getAccountAttribute("name");
        try {
            xmppManager.getConnection().getRoster().createEntry(owner, name, new String[]{groupName});
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 得到数据库当前用户的所有分组
     * @param xmppConnection xmpp连接
     * @return 包含当前用户所有分组的链表
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
        Collection<RosterGroup> rosterGroup = xmppConnection.getRoster().getGroups();
        System.out.println(rosterGroup.size());
        Iterator<RosterGroup> it = rosterGroup.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    /**
     * 得到一个分组里面所有的用户
     * @param xmppConnection xmpp连接
     * @param groupName 组名
     * @return 包含好友的列表
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
     * 获取一个用户的全部好友
     * @param xmppConnection
     * @return 全部好友列表
     */
    public List<RosterEntry> getAllUsers(XMPPConnection xmppConnection) {
        Collection<RosterEntry> entries = xmppConnection.getRoster().getEntries();
        List<RosterEntry> list = new ArrayList<RosterEntry>();
        for (RosterEntry entry: entries) {
            list.add(entry);
        }
        return list;
    }

    /**
     * 根据组名删除该组
     * @param roster roster对象
     * @param groupName 组名
     * @return 成功返回true，失败返回false
     */
    public boolean removeGroup(Roster roster, String groupName) {
        return false;
    }

    /**
     * 不分组添加好友
     * @param userName 好友用户名
     * @param name 好友别名
     * @return 成功返回true，失败返回false
     */
    public boolean addFriend(String userName, String name) {
        if (connection == null) {
            return false;
        }
        try {
            xmppManager.getConnection().getRoster().createEntry(userName, name, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 分组添加好友
     * @param userName 好友用户名
     * @param nickName 好友别名
     * @param groupName 组名
     * @return 成功返回true，失败返回false
     */
    public boolean addFriend(String userName, String nickName, String groupName) {
        if (connection == null) {
            return false;
        }
        try {
            Presence presence = new Presence(Presence.Type.subscribed);
            presence.setTo(userName);
            userName += "@"+xmppManager.getConnection().getServiceName();
            xmppManager.getConnection().sendPacket(presence);
            xmppManager.getConnection().getRoster().createEntry(userName, nickName, new String[]{groupName});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将好友移出列表
     * @param userName 好友用户名
     * @return 成功返回true，失败返回false
     */
    public boolean removeFriend(String userName) {
        if (connection == null) {
            return false;
        }
        try {
            RosterEntry entry = null;
            if (userName.contains("@")) {
                entry = xmppManager.getConnection().getRoster().getEntry(userName);
            }
            else {
                entry = xmppManager.getConnection().getRoster().getEntry(
                        userName + "@" + xmppManager.getConnection().getServiceName());
            }
            if (entry == null) {
                entry = xmppManager.getConnection().getRoster().getEntry(userName);
            }
            xmppManager.getConnection().getRoster().removeEntry(entry);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据好友用户名查找好友
     * @param userName 好友用户名
     * @return 符合要求的好友列表
     */
    public List<HashMap<String,String>> searchUser(String userName) {
        if (connection == null) {
            return null;
        }
        HashMap<String, String> user = null;
        List<HashMap<String,String>> results = new ArrayList<HashMap<String, String>>();
        try {
            //发现服务
            //new ServiceDiscoveryManager(xmppManager.getConnection());
            UserSearchManager userSearchManager = new UserSearchManager(xmppManager.getConnection());
            //这里必须加上search，因为openfire默认的搜索服务器是search.127.0.0.1
            String serviceDomain = "search."+xmppManager.getConnection().getServiceName();
            System.out.println(serviceDomain);
            Form searchForm = userSearchManager.getSearchForm(serviceDomain);
            Form answerForm = searchForm.createAnswerForm();
            //这里设置Username为true表示要在这个里面搜索
            answerForm.setAnswer("Username", true);
            //这里设置search表示要搜索的关键词
            answerForm.setAnswer("search", userName);
            ReportedData data = userSearchManager.getSearchResults(answerForm, serviceDomain);
            Iterator<ReportedData.Row> it = data.getRows();
            ReportedData.Row row = null;
            while(it.hasNext())
            {
                user = new HashMap<String, String>();
                row = it.next();
                user.put("username",row.getValues("Username").next().toString());
                user.put("name",row.getValues("Name").next().toString());
                user.put("email",row.getValues("Email").next().toString());

                results.add(user);
                //若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
            }
            } catch (XMPPException e1) {
            e1.printStackTrace();
        }
        return results;
    }

    /**
     * 与某个好友建立对话
     * @param friend 好友用户名
     * @param listener 消息监听
     * @return 返回已经建立的对话
     */
    public Chat getFriendChat(String friend, MessageListener listener) {
        if (connection == null) {
            return null;
        }
        for (String friendStr: chatManage.keySet()) {
            if (friendStr.equals(friend)) {
                return chatManage.get(friendStr);
            }
        }
        /**
         * 创建对话窗口
         */
        Chat chat = xmppManager.getConnection().getChatManager().createChat(friend + "@" +xmppManager.getConnection().getServiceName(), listener);
        chatManage.put(friend, chat);
        return chat;
    }

    /**
     * 发送消息给好友
     * @param friend 好友用户名
     * @param message 文本消息
     */
    public void sendMessage(String friend, String message) {
        Chat chat = getFriendChat(friend, null);
        try {
            //String messageJson = "{\"messageType\":\""+messageType+"\",\"chanId\":\""+chanId+"\",\"chanName\":\""+chanName+"\"}";
            chat.sendMessage(message);
            System.out.println("From " + xmppManager.getConnection().getUser() + ": "+ message);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给对话添加消息监听
     */
    public void addListener() {
        xmppManager.getConnection().getChatManager().addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean b) {
                chat.addMessageListener(new MessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        StringUtils.parseName(xmppManager.getConnection().getUser());
                        String from = message.getFrom();
                        String content = message.getBody();
                        System.out.println("From " + from + ": "+ content);
                    }
                });
            }
        });
    }

    /**
     * 从服务器获取离线消息
     */
    public void getOfflineMessages() {
        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(xmppManager.getConnection());
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
     * 更改好友的昵称
     * @param friendUserName 好友用户名
     * @param nickName 好友昵称
     */
    public void changeNickName(String friendUserName, String nickName) {
        String user = friendUserName + "@" + Constants.SERVER_NAME;
        RosterEntry entry = xmppManager.getConnection().getRoster().getEntry(user);
        System.out.println(entry.getUser());
        entry.setName(nickName);
    }

    /**
     * 将好友移动到新的分组里面
     * @param friendUserName 好友用户名称
     * @param groupName 分组组名
     */
    public void moveFriendToGroup(String friendUserName, String groupName) {
        String user = friendUserName + "@" + Constants.SERVER_NAME;
        RosterEntry entry = xmppManager.getConnection().getRoster().getEntry(user);
        String nickName = entry.getName();
        System.out.println(addFriend(friendUserName,nickName,groupName));
    }
}
