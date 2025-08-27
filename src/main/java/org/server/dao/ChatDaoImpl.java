package org.server.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class ChatDaoImpl implements ChatDao {

  private final Connection connection;

  public ChatDaoImpl(Connection connection) {
    this.connection = connection;
  }

  @Override
  public Optional<String> getMessage(String groupName) {
    String sql = "SELECT message FROM chat WHERE group_name = ? ORDER BY created_at DESC";
    try (var statement = connection.prepareStatement(sql)){
      statement.setString(1, groupName);
      try (var resultSet = statement.getResultSet()) {
        if (resultSet.next()) {
          return Optional.of(resultSet.getString("message"));
        }
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void save(String groupName, String message, String username) {
    String sql = "INSERT INTO chat(group_name, message, username) VALUES(?, ?, ?)";
    try {
      connection.setAutoCommit(false);

      try (var statement = connection.prepareStatement(sql)) {
        statement.setString(1, groupName);
        statement.setString(2, message);
        statement.setString(3, username);
        statement.executeUpdate();
      }

      connection.commit();
    } catch (SQLException e) {

      try {
        connection.rollback();
      } catch (SQLException rollbackEx) {
        throw new RuntimeException("Rollback failed!", rollbackEx);
      }

      throw new RuntimeException("Save failed!", e);
    } finally {
      try {
        connection.setAutoCommit(true);
      } catch (SQLException e) {
        System.err.println("Warning: Failed to reset autocommit! " + e.getMessage());
      }
    }
  }
}
