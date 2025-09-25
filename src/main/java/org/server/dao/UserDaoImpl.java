package org.server.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import org.server.User;

public record UserDaoImpl(Connection connection) implements UserDao {

  @Override
  public Optional<User> findByUsername(String username) {
    String sql = "SELECT id, username, password FROM users WHERE username = ?";

    try (var statement = connection.prepareStatement(sql)) {
      statement.setString(1, username);

      try (var resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          User user = new User();
          user.setId(resultSet.getLong("id"));
          user.setUsername(resultSet.getString("username"));
          return Optional.of(user);
        } else {
          return Optional.empty();
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error finding user by username: " + username, e);
    }
  }

  @Override
  public void save(User user) {
    String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

    try {
      connection.setAutoCommit(false);

      try (var statement = connection.prepareStatement(sql)) {
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPassword());
        statement.executeUpdate();
      }

      connection.commit();
    } catch (SQLException e) {
      try {
        connection.rollback();
      } catch (SQLException ex) {
        throw new RuntimeException("Rollback failed! ", ex);
      }
      throw new RuntimeException("Save failed! ", e);
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException e) {
        System.out.println("Warning: Failed to reset autocommit! " + e.getMessage());
      }
    }
  }
}
