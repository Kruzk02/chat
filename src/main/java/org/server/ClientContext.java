package org.server;

import java.io.DataOutputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ClientContext {
  private final AtomicReference<String> usernameRef = new AtomicReference<>();
  private final Set<String> joinedGroups = ConcurrentHashMap.newKeySet();
  private final DataOutputStream out;

  public ClientContext(DataOutputStream out) {
    this.out = out;
  }

  public AtomicReference<String> getUsernameRef() {
    return usernameRef;
  }

  public Set<String> getJoinedGroups() {
    return joinedGroups;
  }

  public DataOutputStream getOut() {
    return out;
  }
}
