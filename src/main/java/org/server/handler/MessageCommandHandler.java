package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class MessageCommandHandler implements CommandHandler {
    @Override
    public boolean canHandle(HeaderType type) {
        return type == HeaderType.MESSAGE;
    }

    @Override
    public void handle(byte header, String payload, Context context) throws IOException {
        var parts = payload.substring(1).split(" ", 2);
        if (parts.length < 2) {
            MessageParser.writeMessage(context.getOut(), header, "Invalid message format.");
            return;
        }

        var targetGroup = parts[0];
        var message = parts[1];
        if (!context.getJoinedGroup().contains(targetGroup)) {
            MessageParser.writeMessage(context.getOut(), header, "You are not part of group: " + targetGroup);
            return;
        }

        var groupMembers = context.getGroups().get(targetGroup);
        if (groupMembers != null) {
            for (var entry : groupMembers.entrySet()) {
                if (!entry.getKey().equals(context.getUsername())) {
                    MessageParser.writeMessage(entry.getValue(), header, ("[" + targetGroup + "] " + context.getUsername() + ": " + message));
                }
            }
        }
    }
}
