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

        Map<String, Set<PrintWriter>> groups = new ConcurrentHashMap<>();

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

    private record ClientHandler(Socket clientSocket, Map<String, Set<PrintWriter>> groups) implements Runnable {

        @Override
        public void run() {

            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String currentGroup = null;
                String input;

                while ((input = in.readLine()) != null) {
                    if (input.startsWith("JOIN ")) {
                        currentGroup = input.substring(5);
                        groups.putIfAbsent(currentGroup, ConcurrentHashMap.newKeySet());
                        groups.get(currentGroup).add(out);
                        out.println("Joined group: " + currentGroup);
                    } else if (input.startsWith("MESSAGE ") && currentGroup != null) {
                        String message = input.substring(8);
                        for (var writer : groups.get(currentGroup)) {
                            writer.println("[" + currentGroup + "]: " + message);
                        }
                    } else if (input.equals("exit")){
                        out.println("Good Bye");
                        break;
                    } else {
                        out.println("Unknown command or not in a group.");
                    }
                }


                if (currentGroup != null) {
                    var group = groups.get(currentGroup);
                    group.remove(out);

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
