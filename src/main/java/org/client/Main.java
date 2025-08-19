package org.client;

import java.io.IOException;
import java.util.Scanner;
import org.client.networking.Client;

public class Main {
  public static void main(String[] args) throws IOException {
    Client client = new Client();
    client.start("127.0.0.1", 8080);
    Scanner scanner = new Scanner(System.in);
    while (true) {
      String str = scanner.nextLine();

      client.send(str);

      if (str.equals("E")) {
        client.stop();
        break;
      }
    }
    client.stop();
  }
}
