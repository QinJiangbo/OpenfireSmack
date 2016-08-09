import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.muc.*;

/**
 * Created by Richard on 7/9/15.
 */
public class MultiChatManager {

    XMPPManager xmppManager = new XMPPManager();

    /**
     * create chatting room
     * @param xmppConnection xmpp connection
     * @param roomName room name
     * @param password access password
     * @return muc object
     */
    public MultiUserChat createRoom(XMPPConnection xmppConnection, String roomName,String password) {
        if (xmppManager.getConnection() == null) {
            return null;
        }

        MultiUserChat muc = null;
        try {
            // 创建一个MultiUserChat
            muc = new MultiUserChat(xmppConnection, roomName + "@conference."
                    + xmppConnection.getServiceName());
            // 创建聊天室
            muc.create(roomName);
            // 获得聊天室的配置表单
            Form form = muc.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复
            for (Iterator<FormField> fields = form.getFields(); fields.hasNext();) {
                FormField field = (FormField) fields.next();
                if (!FormField.TYPE_HIDDEN.equals(field.getType())
                        && field.getVariable() != null) {
                    // 设置默认值作为答复
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }
            // 设置聊天室的新拥有者
            List<String> owners = new ArrayList<String>();
            owners.add(xmppManager.getConnection().getUser());// 用户JID
            submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            // 设置聊天室是持久聊天室，即将要被保存下来
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            // 房间最大成员数量
            List<String> maxUsers = new ArrayList<String>();
            maxUsers.add("500");
            submitForm.setAnswer("muc#roomconfig_maxusers", maxUsers);
            // 房间仅对成员开放
            submitForm.setAnswer("muc#roomconfig_membersonly", true);
            // 允许占有者邀请其他人
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            if (!password.equals("")) {
                // 进入是否需要密码
                submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
                // 设置进入密码
                submitForm.setAnswer("muc#roomconfig_roomsecret", password);
            }
            // 能够发现占有者真实 JID 的角色
            // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
            // 登录房间对话
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            // 仅允许注册的昵称登录
            submitForm.setAnswer("x-muc#roomconfig_reservednick", false);
            // 允许使用者修改昵称
            submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);
            // 允许用户注册房间
            submitForm.setAnswer("x-muc#roomconfig_registration", true);
            // 发送已完成的表单（有默认值）到服务器来配置聊天室
            muc.sendConfigurationForm(submitForm);
        } catch (XMPPException e) {
            e.printStackTrace();
            return null;
        }
        return muc;
    }

    /**
     * join the chatting room
     * @param user nickname
     * @param roomsName room name
     * @param password access password
     * @return
     */
    public MultiUserChat joinMultiUserChat(String user, String roomsName,
                                           String password) {
        if (xmppManager.getConnection() == null) {
            return null;
        }
        try {
            // 使用XMPPConnection创建一个MultiUserChat窗口
            MultiUserChat muc = new MultiUserChat(xmppManager.getConnection(), roomsName
                    + "@conference." + xmppManager.getConnection().getServiceName());
            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
            // history.setSince(new Date());
            // 用户加入聊天室
            muc.join(user, password, history,
                    SmackConfiguration.getPacketReplyTimeout());
            System.out.println(user + " joins Chat Room[" + roomsName + "] successfully........");
            return muc;
        } catch (XMPPException e) {
            e.printStackTrace();
            System.out.println(user + " joins Chat Room[" + roomsName + "] failed........");
            return null;
        }
    }

    /**
     * 添加多用户聊天监听
     * @param muc
     */
    public void addListener(MultiUserChat muc) {
        muc.addMessageListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                Message message = (Message) packet;
                String content = message.getBody();
                String from = message.getFrom();
                System.out.println(from.split("/")[1] + ": " + content);
            }
        });
    }

    /**
     * 注册成为聊天室的固定成员
     * @param xmppConnection
     * @param roomName
     * @param nickName
     * @return true表示成功,false表示失败
     */
    public boolean registerMember(XMPPConnection xmppConnection, String roomName, String nickName) {
        String userName = xmppConnection.getUser().split("@")[0];
        MultiUserChat muc = new MultiUserChat(xmppConnection, roomName
                + "@conference." + xmppConnection.getServiceName());
        try {
            // 获得注册的配置表单
            Form registerForm = muc.getRegistrationForm();
            // 根据原表单创建一个新表单
            Form answerForm = registerForm.createAnswerForm();
            answerForm.setAnswer("muc#register_first", userName);
            answerForm.setAnswer("muc#register_last", "");
            answerForm.setAnswer("muc#register_roomnick", nickName);
            answerForm.setAnswer("muc#register_email", "demo@cecesat.com");
            answerForm.setAnswer("muc#register_faqentry", "demo");
            answerForm.setAnswer("muc#register_url", "http://demo.cecesat.com");
            muc.sendRegistrationForm(registerForm);
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 查找聊天室
     * @param xmppConnection
     * @param roomName
     * @return 聊天室信息
     */
    public RoomInfo searchRoom(XMPPConnection xmppConnection, String roomName) {
        String room = roomName + "@conference." + xmppConnection.getServiceName();
        RoomInfo roomInfo = null;
        try {
            roomInfo = MultiUserChat.getRoomInfo(xmppConnection, room);
        } catch (XMPPException e) {
            e.printStackTrace();
            return null;
        }
        return roomInfo;
    }

    /**
     * 更改当前成员在聊天室的昵称
     * @param roomName
     * @param nickName
     * @param xmppConnection
     * @return true表示成功, false表示失败
     */
    public boolean changeNickName(XMPPConnection xmppConnection, String userName,
                                  String accessPassword, String roomName, String nickName) {
        String roomJID = roomName + "@conference." + xmppConnection.getServiceName();
        MultiUserChat muc = new MultiUserChat(xmppConnection, roomJID);
        try {
            muc.join(userName, accessPassword);
            muc.changeNickname(nickName);
            System.out.println(muc.getNickname());
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 添加管理员
     * @param xmppConnection
     * @param roomName
     * @param adminUser
     * @return true表示成功, false表示失败
     */
    public boolean addAdmin(XMPPConnection xmppConnection, String roomName, String adminUser) {
        String jid = adminUser + "@" + xmppConnection.getServiceName();
        String roomJID = roomName + "@conference." + xmppConnection.getServiceName();
        MultiUserChat muc = new MultiUserChat(xmppConnection, roomJID);
        try {
            muc.join(xmppConnection.getUser().split("@")[0], "123456");
            Form form = muc.getConfigurationForm();
            Form submitForm = form.createAnswerForm();
            List<String> admins = new ArrayList<String>();
            admins.add(jid);
            submitForm.setAnswer("muc#roomconfig_roomadmins", admins);
            muc.sendConfigurationForm(form);
            //muc.grantAdmin(jid);
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 销毁聊天室
     * @param xmppConnection
     * @param roomName
     * @return true表示成功, false表示失败
     */
    public boolean destroyChatRoom(XMPPConnection xmppConnection, String roomName, String password){
        String userName = xmppConnection.getUser().split("@")[0];
        String roomJID = roomName + "@conference." + xmppConnection.getServiceName();
        MultiUserChat muc = new MultiUserChat(xmppConnection, roomJID);
        try {
            muc.join(userName, password);
            // destroy这里两个参数设置为默认的值
            muc.destroy("No reason", "cecesat@conference.127.0.0.1");
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取聊天室的成员信息
     * @param xmppConnection
     * @param roomName
     * @param password
     * @return 成员信息的列表
     */
    public List<Affiliate> getMucMembers(XMPPConnection xmppConnection, String roomName, String password) {
        String userName = xmppConnection.getUser().split("@")[0];
        String roomJID = roomName + "@conference." + xmppConnection.getServiceName();
        MultiUserChat muc = new MultiUserChat(xmppConnection, roomJID);
        List<Affiliate> memList = new ArrayList<Affiliate>();
        try {
            muc.join(userName, password);
            Collection<Affiliate> list = muc.getMembers();
            Iterator<Affiliate> iterator = list.iterator();
            while (iterator.hasNext()) {
                memList.add(iterator.next());
            }
        }catch (XMPPException e) {
            e.printStackTrace();
            return null;
        }
        return memList;
    }

    /**
     * 获取已经注册过的聊天室列表
     * @param xmppConnection
     * @return 注册过的聊天室列表
     */
    public Iterator<String> getJoinedRooms(XMPPConnection xmppConnection) {
        String userName = xmppConnection.getUser().split("/")[0];
        System.out.println(userName);
        Iterator<String> iterator = MultiUserChat.getJoinedRooms(xmppConnection, userName);
        return iterator;
    }

    /**
     * 获取公开的聊天室信息
     * @param xmppConnection
     * @return 聊天室信息列表
     */
    public List<HostedRoom> getHostedRooms(XMPPConnection xmppConnection) {
        List<HostedRoom> list = new ArrayList<HostedRoom>();
        Iterator<HostedRoom> iterator = null;
        try {
            // 必须加上conference, "conference.127.0.0.1"
            iterator = MultiUserChat.getHostedRooms(xmppConnection, "conference." + xmppConnection.getServiceName()).iterator();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

}
