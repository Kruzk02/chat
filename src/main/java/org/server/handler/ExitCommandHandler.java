package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.IOException;

public class ExitCommandHandler implements CommandHandler {
    @Override
    public boolean canHandle(HeaderType type) {
        return type == HeaderType.EXIT;
    }

    @Override
    public void handle(byte header, String payload, Context context) throws IOException {
        MessageParser.writeMessage(context.getOut(), header, "Good bye");
        if (context.getUsername() != null) {
            for (var group : context.getJoinedGroup()) {
                var groupMap = context.getGroups().get(group);

                if (groupMap != null) {
                    var writer = groupMap.remove(context.getUsername());
                    if (writer != null) {
                        writer.close();
                    }

                    if (groupMap.isEmpty()) {
                        context.getGroups().remove(group);
                    }
                }
            }
        }
    }
}
