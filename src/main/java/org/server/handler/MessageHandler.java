package org.server.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.HeaderType;
import org.MessageParser;

public class MessageHandler extends AbstractHeaderHandler {
  private final AtomicReference<String> usernameRef;
  private final Set<String> joinedGroups;
  private final Map<String, Map<String, DataOutputStream>> groups;

  public MessageHandler(
      AtomicReference<String> usernameRef,
      Set<String> joinedGroups,
      Map<String, Map<String, DataOutputStream>> groups) {
    this.usernameRef = usernameRef;
    this.joinedGroups = joinedGroups;
    this.groups = groups;
  }

  @Override
  public void handle(HeaderType type, byte header, String payload, DataOutputStream out)
      throws IOException {
    if (type == HeaderType.MESSAGE) {
      var parts = payload.substring(1).split(" ", 2);
      if (parts.length < 2) {
        MessageParser.writeMessage(out, header, "Invalid message format.");
        return;
      }

      var targetGroup = parts[0];
      var message = parts[1];
      var username = usernameRef.get();
      if (!joinedGroups.contains(targetGroup)) {
        MessageParser.writeMessage(out, header, "You are not part of group: " + targetGroup);
        return;
      }

      Map<String, DataOutputStream> groupMembers = groups.get(targetGroup);
      if (groupMembers != null) {
        for (var entry : groupMembers.entrySet()) {
          if (!entry.getKey().equals(username)) {
            MessageParser.writeMessage(
                entry.getValue(), header, ("[" + targetGroup + "] " + username + ": " + message));
          }
        }
      }
    } else {
      super.handle(type, header, payload, out);
    }
  }
}
