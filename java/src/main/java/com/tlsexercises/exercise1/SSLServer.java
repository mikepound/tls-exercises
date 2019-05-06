package com.tlsexercises.exercise1;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.nio.*;

/**
 * Exercise 1 Server
 */
public class SSLServer {

    private static final int LOCAL_PORT = 8282;
    private static final String KEYSTORE_LOCATION = SSLServer.class.getResource("/server/ServerKeyStore.jks").getPath();
    private static final String KEYSTORE_PASSWORD = "experttls";

    // Entry point
    public static void main(String argv[]) throws Exception {
        // You can also update properties as a command line parameter e.g.
        //     -Djavax.net.ssl.keyStore="keystore_location"
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

        SSLServer server = new SSLServer();
        server.startServer();
    }

    // Start server
    public void startServer() {
        try {
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            SSLServerSocket socket = (SSLServerSocket) ssf.createServerSocket(LOCAL_PORT);

            // Part 2
            // Add code here to disable older protocols and cipher suites. You can add similar restrictions
            // to the client code, too.
            //
            // For help check out:
            //      https://github.com/mikepound/tls-exercises/blob/master/java/README.md

            System.out.println(String.format("Listening on port %d...", socket.getLocalPort()));

            while (true) {
                Socket client = socket.accept();
                ClientHandler handler = new ClientHandler(client);
                handler.start();
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e.getMessage());
        }
    }

    /**
     * This nested class handles any new clients that appear. This frees the main thread to continue
     * receiving new clients and performing handshakes.
     *
     * This particular handler sends and receives bytes via buffered input and output streams
     */
    class ClientHandler extends Thread {

        Socket client;
        BufferedInputStream input;
        BufferedOutputStream output;

        public ClientHandler(Socket s) { // constructor
            client = s;

            try {
                input = new BufferedInputStream(client.getInputStream());
                output = new BufferedOutputStream(client.getOutputStream());
            } catch (IOException e) {
                System.err.println("Error creating streams: " + e.getMessage());
            }
        }

        public void run() {
            try {
                // Obtain client message and use it
                byte[] clientMessage = new byte[64];
                int len = input.read(clientMessage, 0, 64);
                int total = 1;
                for (int i = 0; i < len; i++) {
                    total *= clientMessage[i];
                }

                System.out.println("Received from Client: " + Arrays.toString(Arrays.copyOfRange(clientMessage, 0, len)));

                // Return product of client's byte values
                ByteBuffer buff = ByteBuffer.allocate(4).putInt(total);
                output.write(buff.array());
                output.flush();

                // Close streams and socket
                output.close();
                input.close();
                client.close();
            } catch (Exception e) {
                System.err.println("Exception: " + e.getMessage());
            }
        }
    }
}

