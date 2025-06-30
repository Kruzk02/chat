package org.server.handler;

import org.HeaderType;

import java.io.DataOutputStream;
import java.io.IOException;

public interface HeaderHandler {
    void setNext(HeaderHandler next);
    void handle(HeaderType type, byte header, String payload, DataOutputStream out) throws IOException;
}
