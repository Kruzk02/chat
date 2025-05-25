package org.client.networking;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    public void start(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());

        receive();
    }

    public void send(String message) throws IOException {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message must not be null or empty");
        }

        byte header = (byte) message.charAt(0);
        byte[] payload = message.substring(1).getBytes(StandardCharsets.UTF_8);

        out.writeInt(payload.length);
        out.writeByte(header);
        out.write(payload);
        out.flush();
    }

    private void receive() {
        Thread.ofVirtual().start(() -> {
            try {
                while (in != null) {
                    int length = in.readInt();
                    byte header = in.readByte();
                    byte[] data = in.readNBytes(length);

                    String input = new String(data, StandardCharsets.UTF_8);
                    System.out.println("Client received: header=" + (char) header + ", payload=" + input);
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
