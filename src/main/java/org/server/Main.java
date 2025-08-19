package org.server;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
  public static void main(String[] args) throws IOException {
    var url = "jdbc:sqlite:db/chat.db";

    try (var conn = DriverManager.getConnection(url)) {
      System.out.println("Connection to SQLite has been established.");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    Server server = new Server();
    server.start(8080);
  }
}
