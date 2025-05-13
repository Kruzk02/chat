package org.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
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

                String currentGroup = null;
                String username = null;
                String input;

                while ((input = in.readLine()) != null) {
                    if (input.startsWith("USERNAME ")) {
                        username = input.substring(9);
                        out.println("Logged in as: " + username);
                    } else if (input.startsWith("JOIN ")) {
                        if (username == null) {
                            out.println("You must sign username first");
                            continue;
                        }

                        currentGroup = input.substring(5);

                        groups.putIfAbsent(currentGroup, new ConcurrentHashMap<>());
                        groups.get(currentGroup).put(username, out);

                        out.println("Joined group: " + currentGroup);
                    } else if (input.startsWith("MESSAGE ") && currentGroup != null) {
                        String message = input.substring(8);
                        for (var writer : groups.get(currentGroup).values()) {
                            if (!writer.equals(out)) {
                                writer.println(username + " has left the group.");
                            }

                            for (var entry : groups.get(currentGroup).entrySet()) {
                                if (!entry.getKey().equals(username)) {
                                    writer.println("[" + currentGroup + "] " + username + ": " + message);
                                }
                            }
                        }
                    } else if (input.equals("exit")){
                        out.println("Good Bye");
                        break;
                    } else {
                        out.println("Unknown command or not in a group.");
                    }
                }


                if (username != null && currentGroup != null) {
                    var group = groups.get(currentGroup);

                    var writer = group.remove(username);
                    if (writer != null) {
                        writer.close();
                    }

                    if (group.isEmpty()) {
                        groups.remove(currentGroup);
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
