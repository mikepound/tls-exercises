package com.tlsexercises.exercise3;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.Socket;

/**
 * Exercise 3 Server
 */
public class SSLServer {

    private static final int LOCAL_PORT = 8484;
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
     * This particular handler sends and receives objects using Object streams.
     */
    class ClientHandler extends Thread {

        Socket client;
        ObjectInputStream input;
        ObjectOutputStream output;

        public ClientHandler(Socket s) { // constructor
            client = s;

            try {
                output = new ObjectOutputStream(client.getOutputStream());
                input = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                System.err.println("Error creating streams: " + e.getMessage());
            }
        }

        public void run() {
            try {
                // Obtain client message containing bank customer ID
                int idRequest = input.readInt();
                System.out.println("Received from Client: " + idRequest);

                // Send back a customer matching this ID
                BankCustomer exampleCustomer = new BankCustomer(
                        idRequest,
                        "Matthew Jones",
                        "matthew.jones@example.com",
                        84127843,
                        200265,
                        "14 A Lane",
                        "A District",
                        "A Town",
                        "NR1 ABC");

                output.writeObject(exampleCustomer);
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

