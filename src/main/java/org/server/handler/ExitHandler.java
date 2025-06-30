package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ExitHandler extends AbstractHeaderHandler {
    private final AtomicReference<String> usernameRef;
    private final Set<String> joinedGroups;
    private final Map<String, Map<String, DataOutputStream>> groups;

    public ExitHandler(AtomicReference<String> usernameRef, Set<String> joinedGroups, Map<String, Map<String, DataOutputStream>> groups) {
        this.usernameRef = usernameRef;
        this.joinedGroups = joinedGroups;
        this.groups = groups;
    }

    @Override
    public void handle(HeaderType type, byte header, String payload, DataOutputStream out) throws IOException {
        if (type == HeaderType.EXIT) {
            var username = usernameRef.get();
            MessageParser.writeMessage(out, header, "Good bye");
            if (username != null) {
                for (var group : joinedGroups) {
                    var groupMap = groups.get(group);

                    if (groupMap != null) {
                        var writer = groupMap.remove(username);
                        if (writer != null) {
                            writer.close();
                        }

                        if (groupMap.isEmpty()) {
                            groups.remove(group);
                        }
                    }
                }
            }
        } else {
            super.handle(type, header, payload, out);
        }
    }
}
