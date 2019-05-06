package com.tlsexercises.exercise2;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Exercise 2 Server\
 */
public class SSLServer {

    private static final int LOCAL_PORT = 8383;
    private static final String KEYSTORE_LOCATION = SSLServer.class.getResource("/server/ServerKeyStore.jks").getPath();
    private static final String KEYSTORE_PASSWORD = "experttls";
    private static final String TRUSTSTORE_LOCATION = SSLServer.class.getResource("/server/ServerTrustStore.jks").getPath();
    private static final String TRUSTSTORE_PASSWORD = "experttls"; // In a real situation these would have different passwords

    // Entry point
    public static void main(String argv[]) throws Exception {
        // You can also update properties as a command line parameter e.g.
        //     -Djavax.net.ssl.keyStore="keystore_location"
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_LOCATION);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);

        SSLServer server = new SSLServer();
        server.startServer();
    }

    // Start server
    public void startServer() {
        try {
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            SSLServerSocket socket = (SSLServerSocket) ssf.createServerSocket(LOCAL_PORT);

            // Require client authentication
            socket.setNeedClientAuth(true);

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

