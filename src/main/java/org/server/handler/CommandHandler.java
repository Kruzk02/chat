package org.server.handler;

import org.HeaderType;

import java.io.IOException;

public interface CommandHandler {
    boolean canHandle(HeaderType type);
    void handle(byte header, String payload, Context context) throws IOException;
}
