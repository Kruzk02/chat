package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class JoinCommandHandler implements CommandHandler {
    @Override
    public boolean canHandle(HeaderType type) {
        return type == HeaderType.JOIN;
    }

    @Override
    public void handle(byte header, String payload, Context context) throws IOException {
        var username = context.getUsername();
        if (username == null) {
            MessageParser.writeMessage(context.getOut(), header, "You must sign username first");
            return;
        }

        var groupName = payload.substring(1);
        var isJoined = context.getJoinedGroup().add(groupName);
        if (!isJoined) {
            MessageParser.writeMessage(context.getOut(), header, "You already in group");
            return;
        }

        context.getGroups().computeIfAbsent(groupName, k -> new ConcurrentHashMap<>()).putIfAbsent(username, context.getOut());

        MessageParser.writeMessage(context.getOut(), header, groupName);
    }
}
