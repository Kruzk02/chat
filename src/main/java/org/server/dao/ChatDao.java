package org.server.dao;

import java.util.List;

public interface ChatDao {
  List<String> getMessages(String groupName);
  void save(String groupName, String message, String username);
}
