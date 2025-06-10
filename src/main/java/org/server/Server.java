package org.server;

import org.HeaderType;
import org.MessageParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

                String username = null;
                Set<String> joinedGroup = ConcurrentHashMap.newKeySet();

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
                    switch (headerType) {
                        case USERNAME -> {
                            username = payload.substring(1);
                            MessageParser.writeMessage(out, header, username);
                        }
                        case JOIN -> {
                            if (username == null) {
                                MessageParser.writeMessage(out, header, "You must sign username first");
                                continue;
                            }

                            String groupName = payload.substring(1);
                            boolean isJoined = joinedGroup.add(groupName);
                            if (!isJoined) {
                                MessageParser.writeMessage(out, header, "You already in group");
                                continue;
                            }

                            groups.computeIfAbsent(groupName, k -> new ConcurrentHashMap<>()).putIfAbsent(username, out);

                            MessageParser.writeMessage(out, header, groupName);
                        }
                        case MESSAGE -> {
                            String[] parts = payload.substring(1).split(" ", 2);
                            if (parts.length < 2) {
                                MessageParser.writeMessage(out, header, "Invalid message format.");
                                continue;
                            }

                            String targetGroup = parts[0];
                            String message = parts[1];
                            if (!joinedGroup.contains(targetGroup)) {
                                MessageParser.writeMessage(out, header, "You are not part of group: " + targetGroup);
                                continue;
                            }

                            Map<String, DataOutputStream> groupMembers = groups.get(targetGroup);
                            if (groupMembers != null) {
                                for (var entry : groupMembers.entrySet()) {
                                    if (!entry.getKey().equals(username)) {
                                        MessageParser.writeMessage(entry.getValue(), header, ("[" + targetGroup + "] " + username + ": " + message));
                                    }
                                }
                            }
                        }
                        case EXIT -> {
                            MessageParser.writeMessage(out, header, "Good bye");
                            if (username != null) {
                                for (var group : joinedGroup) {
                                    var groupMap = groups.get(group);

                                    if (groupMap != null) {
                                        var writer = groupMap.remove(username);
                                        if (writer != null) {
                                            writer.close();
                                        }

                                        if (groupMap.isEmpty()) {
                                            groups.remove(group);
                                        }
                                    }
                                }
                            }
                        }
                        default -> MessageParser.writeMessage(out, header, "Unknown command or not in group.");
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
