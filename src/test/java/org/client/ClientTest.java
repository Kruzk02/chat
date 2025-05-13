package org.client;

import org.client.networking.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private Client client;

    @BeforeEach
    void setUp() throws IOException {
        client = new Client();
        client.start("127.0.0.1", 8080);
    }

    @Test
    public void testHello_whenServerResponseWhenStarted() throws IOException {
        client.send("JOIN HELLO_WORLD");
        client.send("MESSAGE HELLO_WORLD");
        client.send("exit");
    }

    @AfterEach
    void tearDown() throws IOException {
        client.stop();
    }
}