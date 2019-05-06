package com.tlsexercises.exercise3;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Exercise 3 "Imposter" Server
 */
public class SSLImposterServer {

    private static final int LOCAL_PORT = 8484;
    private static final String KEYSTORE_LOCATION = SSLImposterServer.class.getResource("/server/ServerAltKeyStore.jks").getPath();
    private static final String KEYSTORE_PASSWORD = "experttls";

    // Entry point
    public static void main(String argv[]) throws Exception {
        // You can also update properties as a command line parameter e.g.
        //     -Djavax.net.ssl.keyStore="keystore_location"
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

        SSLImposterServer server = new SSLImposterServer();
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
     * This client handler sends and receives objects using Object streams.
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
                        "Imposter data",
                        "not.real@email.com",
                        123456789,
                        654321,
                        "Nowhere",
                        "",
                        "",
                        "");

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

