package org.server.handler;

import org.HeaderType;
import org.MessageParser;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractHeaderHandler implements HeaderHandler {

    private HeaderHandler next;

    @Override
    public void setNext(HeaderHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HeaderType type, byte header, String payload, DataOutputStream out) throws IOException {
        if (next != null) {
            next.handle(type, header, payload, out);
        } else {
            MessageParser.writeMessage(out, header, "Unknown command or not in group.");
        }
    }
}
