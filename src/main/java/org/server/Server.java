package org.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String str = in.readLine();
        if (str.equals("Hello server")) {
            out.println("Hello client");
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
        clientSocket.close();
        out.close();
        in.close();
    }
}
