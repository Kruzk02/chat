package org.server;

import org.HeaderType;
import org.MessageParser;
import org.server.handler.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

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
        serverSocket.close();
    }

    private record ClientHandler(Socket clientSocket, Map<String, Map<String, DataOutputStream>> groups) implements Runnable {

        @Override
        public void run() {
            try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                 DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {

                List<CommandHandler> handlers = List.of(
                        new UsernameCommandHandler(),
                        new JoinCommandHandler(),
                        new MessageCommandHandler(),
                        new ExitCommandHandler()
                );

                Context context = new Context();
                context.setOut(out);
                context.setJoinedGroup(ConcurrentHashMap.newKeySet());
                context.setGroups(groups);

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

                    boolean handled = false;
                    for (var handler : handlers) {
                        if (handler.canHandle(headerType)) {
                            handler.handle(header, payload, context);
                            handled = true;
                            break;
                        }
                    }

                    if (!handled) {
                        MessageParser.writeMessage(out, header, "Unknown command.");
                    }
                }

            } catch (IOException e) {
                System.err.println("Client connection error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
