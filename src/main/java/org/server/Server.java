package org.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (SocketException e) {
            System.out.println("Server socket closed.");
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private record ClientHandler(Socket clientSocket) implements Runnable {

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.equals("exit")) {
                        out.println("good bye");
                        break;
                    }
                    out.println(input);
                }
            } catch (IOException e) {
                System.err.println("Client connection error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
