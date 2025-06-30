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
import java.util.concurrent.atomic.AtomicReference;

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

    private record ClientHandler(Socket clientSocket, Map<String, Map<String, DataOutputStream>> groups) implements Runnable {

        @Override
        public void run() {
            try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                 DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {

                HeaderHandler usernameHandler = getHeaderHandler();

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
                    usernameHandler.handle(headerType, header, payload, out);
                }

            } catch (IOException e) {
                System.err.println("Client connection error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException ignored) {}
            }
        }

        private HeaderHandler getHeaderHandler() {
            AtomicReference<String> usernameRef = new AtomicReference<>();
            Set<String> joinedGroup = ConcurrentHashMap.newKeySet();

            HeaderHandler usernameHandler = new UsernameHandler(usernameRef);
            HeaderHandler joinHandler = new JoinHandler(usernameRef, joinedGroup, groups);
            HeaderHandler messageHandler = new MessageHandler(usernameRef, joinedGroup, groups);
            HeaderHandler exitHandler = new ExitHandler(usernameRef, joinedGroup, groups);

            usernameHandler.setNext(joinHandler);
            joinHandler.setNext(messageHandler);
            messageHandler.setNext(exitHandler);

            return usernameHandler;
        }
    }
}
