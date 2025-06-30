package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class UsernameHandler extends AbstractHeaderHandler {

    private final AtomicReference<String> usernameRef;

    public UsernameHandler(AtomicReference<String> usernameRef) {
        this.usernameRef = usernameRef;
    }

    @Override
    public void handle(HeaderType type, byte header, String payload, DataOutputStream out) throws IOException {
        if (type == HeaderType.USERNAME) {
            var username = payload.substring(1);
            usernameRef.set(username);
            MessageParser.writeMessage(out, header, username);
        } else {
            super.handle(type, header, payload, out);
        }
    }
}
