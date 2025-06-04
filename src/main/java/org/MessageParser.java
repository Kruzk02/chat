package org;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for serializing and deserializing simple messages
 * with a header and a UTF-8 encoded payload.
 *
 * <p>A message consists of:
 * <ul>
 *   <li>1 byte header</li>
 *   <li>4-byte integer representing payload length</li>
 *   <li>Payload as UTF-8 encoded bytes</li>
 * </ul>
 */
public class MessageParser {

    /**
     * Record representing a parsed message.
     *
     * @param header The message header (1 byte).
     * @param payload The message payload as a string.
     */
    public record Message(byte header, String payload) {}


    /**
     * Writes a message to the given {@link DataOutputStream}.
     * The message is composed of a 1-byte header, a 4-byte length of the payload,
     * and the UTF-8 encoded payload bytes.
     *
     * @param out The {@code DataOutputStream} to write the message to.
     * @param header The 1-byte header of the message.
     * @param payload The UTF-8 string payload of the message.
     * @throws IOException If an I/O error occurs while writing to the stream.
     */
    public static void writeMessage(DataOutputStream out, byte header, String payload) throws IOException {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);

        out.writeByte(header);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    /**
     * Reads a message from the given {@link DataInputStream}.
     * Expects the message format to be:
     * <ul>
     *   <li>1 byte header</li>
     *   <li>4 bytes indicating the length of the payload</li>
     *   <li>UTF-8 encoded payload bytes</li>
     * </ul>
     *
     * @param in The {@code DataInputStream} to read the message from.
     * @return A {@link Message} object containing the header and payload.
     * @throws IOException If an I/O error occurs or the end of the stream is reached unexpectedly.
     */
    public static Message readMessage(DataInputStream in) throws IOException {
        byte header = in.readByte();
        int length = in.readInt();
        byte[] payload = in.readNBytes(length);
        if (payload.length != length) {
            throw new EOFException("Unexpected end of stream");
        }
        return new Message(header, new String(payload, StandardCharsets.UTF_8));
    }
}
