package org.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
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
                    int length;
                    try {
                        length = in.readInt();
                    } catch (EOFException e) {
                        System.out.println("Client disconnected.");
                        break;
                    }

                    byte header = in.readByte();
                    byte[] payload = in.readNBytes(length);

                    String input = new String(payload, StandardCharsets.UTF_8);
                    System.out.println("Server received: header=" + (char) header + ", payload=" + input);

                    switch (header) {
                        case 'U' -> {
                            username = input.substring(1);
                            sendResponse(out, header, input);
                        }
                        case 'J' -> {
                            if (username == null) {
                                sendResponse(out, header, "You must sign username first");
                                continue;
                            }

                            String groupName = input.substring(1);

                            groups.putIfAbsent(groupName, new ConcurrentHashMap<>());
                            groups.get(groupName).put(username, out);

                            joinedGroup.add(groupName);

                            sendResponse(out, header, groupName);
                        }
                        case 'M'-> {
                            String[] parts = input.substring(1).split(" ", 2);
                            if (parts.length < 2) {
                                sendResponse(out, header, "Invalid message format.");
                                continue;
                            }

                            String targetGroup = parts[0];
                            String message = parts[1];

                            if (!joinedGroup.contains(targetGroup)) {
                                sendResponse(out, header, "You are not part of group: " + targetGroup);
                                continue;
                            }

                            Map<String, DataOutputStream> groupMembers = groups.get(targetGroup);
                            if (groupMembers != null) {
                                for (var entry : groupMembers.entrySet()) {
                                    if (!entry.getKey().equals(username)) {
                                        sendResponse(entry.getValue(), header, ("[" + targetGroup + "] " + username + ": " + message));
                                    }
                                }
                            }
                        }
                        case 'E' -> {
                            sendResponse(out, header, "Good bye");

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
                        default -> sendResponse(out, header, "Unknow command or not in group.");
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

        private void sendResponse(DataOutputStream out, byte header, String response) {
            try {
                byte[] responsePayload = response.getBytes(StandardCharsets.UTF_8);
                out.writeInt(responsePayload.length);
                out.writeByte(header);
                out.write(responsePayload);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
