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
      if (str.equals("U LOGIN")) {

        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        if (username.contains(" ")) {
          System.out.println("Username shouldn't contain space.");
          return;
        }

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        if (password.contains(" ")) {
          System.out.println("Password shouldn't contain space.");
          return;
        }

        client.send(str + " " + username + " " + password);
      } else if (str.equals("U REGISTER")) {

        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        if (username.contains(" ")) {
          System.out.println("Username shouldn't contain space.");
          return;
        }

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();
        if (password.contains(" ")) {
          System.out.println("Password shouldn't contain space.");
          return;
        }

        client.send(str + " " + username + " " + password);
      } else {
        client.send(str);
      }


      if (str.equals("E")) {
        client.stop();
        break;
      }
    }
    client.stop();
  }
}
