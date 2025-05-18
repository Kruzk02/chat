package org.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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

        Map<String, Map<String, PrintWriter>> groups = new ConcurrentHashMap<>();

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

    private record ClientHandler(Socket clientSocket, Map<String, Map<String, PrintWriter>>  groups) implements Runnable {

        @Override
        public void run() {

            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String username = null;
                String input;

                Set<String> joinedGroup = ConcurrentHashMap.newKeySet();

                while ((input = in.readLine()) != null) {
                    if (input.startsWith("USERNAME ")) {
                        username = input.substring(9);
                        out.println("Logged in as: " + username);
                    } else if (input.startsWith("JOIN ")) {
                        if (username == null) {
                            out.println("You must sign username first");
                            continue;
                        }

                        String groupName = input.substring(5);

                        groups.putIfAbsent(groupName, new ConcurrentHashMap<>());
                        groups.get(groupName).put(username, out);

                        joinedGroup.add(groupName);
                        out.println("Joined group: " + groupName);

                    } else if (input.startsWith("MESSAGE ") && !joinedGroup.isEmpty()) {
                        String[] parts = input.substring(8).split(" ", 2);
                        if (parts.length < 2) {
                            out.println("Invalid message format.");
                            continue;
                        }

                        String targetGroup = parts[0];
                        String message = parts[1];

                        if (!joinedGroup.contains(targetGroup)) {
                            out.println("You are not part of group: " + targetGroup);
                            continue;
                        }

                        Map<String, PrintWriter> groupMembers = groups.get(targetGroup);
                        if (groupMembers != null) {
                            for (var entry : groupMembers.entrySet()) {
                                if (!entry.getKey().equals(username)) {
                                    entry.getValue().println("[" + targetGroup + "] " + username + ": " + message);
                                }
                            }
                        }
                    } else if (input.equals("exit")){
                        out.println("Good Bye");

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
                        break;
                    } else {
                        out.println("Unknown command or not in a group.");
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
