package org.server.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import org.HeaderType;

public interface HeaderHandler {
  void setNext(HeaderHandler next);

  void handle(HeaderType type, byte header, String payload, DataOutputStream out)
      throws IOException;
}
