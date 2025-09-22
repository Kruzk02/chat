package org.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private static DatabaseConnection instance;
  private final Connection connection;

  private DatabaseConnection() throws SQLException {
    this.connection = DriverManager.getConnection("jdbc:sqlite:db/chat.db");

    var chatTable = "CREATE TABLE IF NOT EXISTS chat ("
        + "id INTEGER PRIMARY KEY, "
        + "user_id NOT NULL,"
        + "group_name varchar(255) NOT NULL,"
        + "message TEXT NOT NULL,"
        + "created_at TIMESTAMP default CURRENT_TIMESTAMP,"
        + "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE"
        + ");";

    var userTable = "CREATE TABLE IF NOT EXISTS users("
        + "id INTEGER PRIMARY KEY, "
        + "username varchar(128) NOT NULL UNIQUE,"
        + "password varchar(255) NOT NULL,"
        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
        + ");";

    var stmt = this.connection.createStatement();
    stmt.execute(userTable);
    stmt.execute(chatTable);
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
