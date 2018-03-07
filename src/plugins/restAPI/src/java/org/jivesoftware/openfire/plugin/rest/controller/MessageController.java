package org.jivesoftware.openfire.plugin.rest.controller;

import javax.ws.rs.core.Response;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.plugin.rest.entity.MessageEntity;
import org.jivesoftware.openfire.plugin.rest.exceptions.ExceptionType;
import org.jivesoftware.openfire.plugin.rest.exceptions.ServiceException;
import org.xmpp.packet.Message;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.util.JiveGlobals;

/**
 * The Class MessageController.
 */
public class MessageController {
    /**
     * The Constant INSTANCE.
     */
    public static final MessageController INSTANCE = new MessageController();

    /**
     * Gets the single instance of MessageController.
     *
     * @return single instance of MessageController
     */
    public static MessageController getInstance() {
        return INSTANCE;
    }

    /**
     * Send broadcast message.
     *
     * @param messageEntity the message entity
     * @throws ServiceException the service exception
     */
    public void sendBroadcastMessage(MessageEntity messageEntity) throws ServiceException {
        if (messageEntity.getBody() != null && !messageEntity.getBody().isEmpty()) {
            try {
                Message message = new Message();
                message.setType(Message.Type.chat);
                message.setBody(messageEntity.getBody());
                String xmppdomain = "@" + JiveGlobals.getProperty("xmpp.domain");
                message.setFrom("system" + xmppdomain);//目前不加from则会导致客户端不能自动获取离线消息，除主动获取。

                XMPPServer server = XMPPServer.getInstance();
                UserManager userManager = server.getUserManager();
                PresenceManager presenceManager = server.getPresenceManager();

                for (User user : UserManager.getInstance().getUsers()) {
                    String username = user.getUsername();
                    message.setTo(username + xmppdomain);
                    if (presenceManager.isAvailable(user)) {
                        server.getRoutingTable().broadcastPacket(message, false);
                    } else {
                        server.getOfflineMessageStrategy().storeOffline(message);
                    }
                }
            } catch (Exception e) {
            }
        } else {
            throw new ServiceException("Message content/body is null or empty", "",
                ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION,
                Response.Status.BAD_REQUEST);
        }
    }

}
