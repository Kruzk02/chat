package org.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Map;
import org.HeaderType;
import org.MessageParser;
import org.server.dao.ChatDao;
import org.server.dao.ChatDaoImpl;
import org.server.handler.HandlerFactory;
import org.server.handler.HeaderHandler;

public record ClientHandler(
    Socket clientSocket, Map<String, Map<String, DataOutputStream>> groups) implements Runnable {

  @Override
  public void run() {
    try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {

      ClientContext context = new ClientContext(out);
      ChatDao chatDao = new ChatDaoImpl(DatabaseConnection.getInstance().getConnection());
      HeaderHandler handler = new HandlerFactory(groups, chatDao).create(context);

      while (true) {
        MessageParser.Message data;
        try {
          data = MessageParser.readMessage(in);
        } catch (IOException e) {
          System.out.println("Client disconnected.");
          break;
        }

        byte header = data.header();
        String payload = data.payload();
        System.out.println("Server received: header=" + header + ", payload=" + payload);

        HeaderType headerType = HeaderType.matchFirstCharacter((char) header);
        handler.handle(headerType, header, payload, out);
      }

    } catch (IOException | SQLException e) {
      System.err.println("Client connection error: " + e.getMessage());
    } finally {
      try {
        clientSocket.close();
      } catch (IOException ignored) {
      }
    }
  }
}
