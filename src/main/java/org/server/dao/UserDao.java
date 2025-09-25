package org.server.dao;

import java.util.Optional;
import org.server.User;

public interface UserDao {
  Optional<User> findByUsername(String username);

  void save(User user);
}
