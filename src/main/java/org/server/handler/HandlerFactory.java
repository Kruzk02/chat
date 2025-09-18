package org.server.handler;

import java.io.DataOutputStream;
import java.util.List;
import java.util.Map;
import org.server.ClientContext;
import org.server.ExpiringCache;
import org.server.dao.ChatDao;

public record HandlerFactory(Map<String, Map<String, DataOutputStream>> groups, ChatDao chatDao) {

  public HeaderHandler create(ClientContext context) {
    var username = context.getUsernameRef();
    var joinedGroups = context.getJoinedGroups();
    var expiringCache = new ExpiringCache<String, List<String>>(60000);

    var usernameHandler = new UsernameHandler(username);
    var joinHandler = new JoinHandler(username, joinedGroups, groups);
    var messageHandler = new MessageHandler(username, joinedGroups, groups, chatDao);
    var getHandler = new GetHandler(chatDao, joinedGroups, expiringCache);
    var exitHandler = new ExitHandler(username, joinedGroups, groups);

    usernameHandler.setNext(joinHandler);
    joinHandler.setNext(messageHandler);
    messageHandler.setNext(getHandler);
    getHandler.setNext(exitHandler);

    return usernameHandler;
  }
}
