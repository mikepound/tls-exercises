package com.tlsexercises.exercise2;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Exercise 2 Server
 */
public class SSLServer {

    private static final int LOCAL_PORT = 8383;
    private static final String KEYSTORE_LOCATION = SSLServer.class.getResource("/server/ServerKeyStore.jks").getPath();
    private static final String KEYSTORE_PASSWORD = "experttls";

    // Entry point
    public static void main(String argv[]) throws Exception {
        // You can also update properties as a command line parameter e.g.
        //     -Djavax.net.ssl.keyStore="keystore_location"
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

        // Part 1
        // In order to verify the client, this server requires a trust store. Use setProperty commands to
        // load a trustStore. You'll also need some static variables above as with the keystore implementation.
        //
        // For help check out:
        //      https://github.com/mikepound/tls-exercises/blob/master/java/README.md

        SSLServer server = new SSLServer();
        server.startServer();
    }

    // Start server
    public void startServer() {
        try {
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            SSLServerSocket socket = (SSLServerSocket) ssf.createServerSocket(LOCAL_PORT);

            // Part 1
            // Add code here to enable client-side authentication. The server will then request the client
            // certificate during the handshake
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
     * This client handler sends and receives strings via BufferedReader and BufferedWriter
     */
    class ClientHandler extends Thread {

        Socket client;
        BufferedReader input;
        BufferedWriter output;

        public ClientHandler(Socket s) { // constructor
            client = s;

            try {
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            } catch (IOException e) {
                System.err.println("Error creating streams: " + e.getMessage());
            }
        }

        public void run() {
            try {
                // Obtain client message and use it
                String request = input.readLine();
                System.out.println("Received from Client: " + request);

                output.write("HTTP/1.0 200 OK\nContent-type: text/html\n" +
                        "<html>\n" +
                        "  <head>\n" +
                        "    <title>Tiny Website</title>\n" +
                        "  </head>\n" +
                        "  <body>\n" +
                        "    A very small website!\n" +
                        "  </body>\n" +
                        "</html>\n");
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

