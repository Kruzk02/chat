package org.client.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        receive();
    }

    public void send(String message) {
        out.println(message);
    }

    private void receive() {
        Thread.ofVirtual().start(() -> {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println("Server: " + response);
                }
            } catch (IOException e) {
                if ("Stream closed".equals(e.getMessage())) {
                    System.out.println("Connection closed normally.");
                } else {
                    System.err.println("Connection closed or error reading: " + e.getMessage());
                }
            } finally {
                try {
                    stop();
                } catch (IOException ignored) { }
            }
        });
    }

    public void stop() throws IOException {
        if (clientSocket != null) clientSocket.close();
        if (out != null) out.close();
        if (in != null) in.close();
    }
}
