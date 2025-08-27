package org.server.dao;

import java.util.Optional;

public interface ChatDao {
  Optional<String> getMessage(String groupName);
  void save(String groupName, String message, String username);
}
