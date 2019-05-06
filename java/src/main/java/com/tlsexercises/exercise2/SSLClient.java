package com.tlsexercises.exercise2;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Exercise 2 Client
 */
public class SSLClient {

    private static final String REMOTE_HOST = "localhost";
    private static final int REMOTE_PORT = 8383;
    private static final String TRUSTSTORE_LOCATION = SSLClient.class.getResource("/client/ClientTrustStore.jks").getPath();
    private static final String TRUSTSTORE_PASSWORD = "experttls";

    // Entry point
    public static void main(String[] args) {
        // You can also update properties as a command line parameter e.g.
        //     -Djavax.net.ssl.keyStore="keystore_location"
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_LOCATION);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);

        // Part 2
        // The server is now expecting a certificate, add relevant keystore definitions and properties above.
        //
        // For help check out:
        //      https://github.com/mikepound/tls-exercises/blob/master/java/README.md

        // Obtain the default socket factory
        SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try {
            // Create a socket - will not connect yet
            SSLSocket socket = (SSLSocket) f.createSocket(REMOTE_HOST, REMOTE_PORT);

            // Handshake to create a session
            socket.startHandshake();

            // What parameters were established?
            System.out.println(String.format("Negotiated Session: %s", socket.getSession().getProtocol()));
            System.out.println(String.format("Cipher Suite: %s", socket.getSession().getCipherSuite()));

            // We're reading and writing characters this time
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            output.write("GET /index.html HTTP/1.1\n");
            output.flush();

            System.out.println("\nServer Response:");
            String currentLine = null;
            System.out.println(input.readLine());
            while ((currentLine = input.readLine()) != null) {
                System.out.println(currentLine);
            }

            input.close();
            output.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e.toString());
        }
    }
}