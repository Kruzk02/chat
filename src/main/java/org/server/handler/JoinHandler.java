package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class JoinHandler extends AbstractHeaderHandler {
    private final AtomicReference<String> usernameRef;
    private final Set<String> joinedGroups;
    private final Map<String, Map<String, DataOutputStream>> groups;

    public JoinHandler(AtomicReference<String> usernameRef, Set<String> joinedGroups, Map<String, Map<String, DataOutputStream>> groups) {
        this.usernameRef = usernameRef;
        this.joinedGroups = joinedGroups;
        this.groups = groups;
    }

    @Override
    public void handle(HeaderType type, byte header, String payload, DataOutputStream out) throws IOException {
        if (type == HeaderType.JOIN) {
            var username = usernameRef.get();
            if (username == null) {
                MessageParser.writeMessage(out, header, "You must sign username first");
            }

            var groupName = payload.substring(1);
            var isJoined = joinedGroups.add(groupName);
            if (!isJoined) {
                MessageParser.writeMessage(out, header, "You already in group");
            }

            groups.computeIfAbsent(groupName, k -> new ConcurrentHashMap<>()).putIfAbsent(username, out);
            MessageParser.writeMessage(out, header, groupName);
        } else {
            super.handle(type, header, payload, out);
        }
    }
}
