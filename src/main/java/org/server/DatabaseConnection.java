package org.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static DatabaseConnection instance;
  private final Connection connection;

  private DatabaseConnection() throws SQLException {
    this.connection = DriverManager.getConnection("jdbc:sqlite:db/chat.db");

    var table = "CREATE TABLE IF NOT EXISTS chat ("
        + "id INTEGER PRIMARY KEY, "
        + "username varchar(128) NOT NULL,"
        + "group_name varchar(255) NOT NULL,"
        + "message TEXT NOT NULL,"
        + "created_at TIMESTAMP default CURRENT_TIMESTAMP"
        + ");";

    var stmt = this.connection.createStatement();
    stmt.execute(table);
  }

  public Connection getConnection() {
    return connection;
  }

  public static DatabaseConnection getInstance() throws SQLException {
    if (instance == null || instance.getConnection().isClosed()) {
      instance = new DatabaseConnection();
    }
    return instance;
  }
}
