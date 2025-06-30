package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.IOException;

public class UsernameCommandHandler implements CommandHandler {
    @Override
    public boolean canHandle(HeaderType type) {
        return type == HeaderType.USERNAME;
    }

    @Override
    public void handle(byte header, String payload, Context context) throws IOException {
        context.setUsername(payload.substring(1));
        MessageParser.writeMessage(context.getOut(), header, context.getUsername());
    }
}
