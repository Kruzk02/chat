package org.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.server.handler.*;

public class Server {
  private ServerSocket serverSocket;
  private final ExecutorService pool =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

  public void start(int port) throws IOException {
    serverSocket = new ServerSocket(port);

    Map<String, Map<String, DataOutputStream>> groups = new ConcurrentHashMap<>();

    try {
      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        pool.execute(new ClientHandler(clientSocket, groups));
      }
    } catch (SocketException e) {
      System.out.println("Server socket closed.");
    }
  }

  public void stop() throws IOException {
    if (!serverSocket.isClosed()) serverSocket.close();
    pool.shutdown();
  }
}
