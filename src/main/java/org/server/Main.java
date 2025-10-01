package org.server;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
  public static void main(String[] args) throws IOException, SQLException {
    Server server = new Server();
    server.start(8080);
  }
}
