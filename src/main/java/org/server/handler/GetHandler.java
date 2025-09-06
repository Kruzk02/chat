package org.server.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.HeaderType;
import org.MessageParser;
import org.server.dao.ChatDao;

public class GetHandler extends AbstractHeaderHandler {

  private final ChatDao chatDao;
  private final Set<String> joinedGroups;

  public GetHandler(ChatDao chatDao, Set<String> joinedGroups) {
    this.chatDao = chatDao;
    this.joinedGroups = joinedGroups;
  }

  @Override
  public void handle(HeaderType type, byte header, String payload, DataOutputStream out)
      throws IOException {
    if (type == HeaderType.GET) {
      var parts = payload.substring(1).split("", 1);
      if (parts.length < 1) {
        MessageParser.writeMessage(out, header, "Invalid message format.");
        return;
      }

      var targetGroup = parts[0];
      if (!joinedGroups.contains(targetGroup)) {
        MessageParser.writeMessage(out, header, "You are no part of group: " + targetGroup);
        return;
      }

      List<String> messages = chatDao.getMessages(targetGroup);
      for (int i = messages.size() - 1; i >= 0;i--) {
        MessageParser.writeMessage(out, header, messages.get(i));
      }
    } else {
      super.handle(type,header,payload,out);
    }
  }
}
