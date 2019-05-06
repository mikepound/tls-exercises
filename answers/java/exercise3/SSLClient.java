package com.tlsexercises.exercise3;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * Exercise 3 Client
 */
public class SSLClient {

    private static final String REMOTE_HOST = "localhost";
    private static final int REMOTE_PORT = 8484;
    private static final String TRUSTSTORE_LOCATION = SSLClient.class.getResource("/client/ClientTrustStore.jks").getPath();
    private static final String TRUSTSTORE_PASSWORD = "experttls";

    private static final byte[] SERVER_PINNED_HASH;
    static {
        SERVER_PINNED_HASH = new byte[32];
        try {
            SSLClient.class.getResourceAsStream("/client/pinned.hash").read(SERVER_PINNED_HASH,0,32);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Unable to load server pinset.");
        }
    }

    // Entry point
    public static void main(String[] args) {
        // You can also update properties as a command line parameter e.g.
        //     -Djavax.net.ssl.keyStore="keystore_location"
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_LOCATION);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);

        TrustManagerFactory tmf = null;

        try {
            // Default is likely to be PKIX, but could be SunX509 on some systems
            String def = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(def);

            // Using null here initialises the default trust store, which in this case is loaded from the above properties
            tmf.init((KeyStore) null);
        } catch (KeyStoreException e) {
            System.err.println("Keystore Exception: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Unable to obtain a trust manager: " + e.getMessage());
        }

        // Get hold of the default trust manager - will only return one as we only specified one algorithm above.
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];

        // Wrap it in your own class.
        X509TrustManager pinningX509TrustManager = new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                // Pass through
                return defaultTrustManager.getAcceptedIssuers();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                // Use default trust manager to verify certificate chain
                defaultTrustManager.checkServerTrusted(chain, authType);

                // The first certificate in the chain
                X509Certificate serverCert = chain[0];

                // .getEncoded returns a DER encoding of the certificate, which should match our pinned hash
                byte[] certBytes = serverCert.getEncoded();

                // Hash the certificate
                byte[] serverHash = null;
                MessageDigest digest = null;
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                    serverHash = digest.digest(certBytes);
                } catch (NoSuchAlgorithmException e) {
                    throw new CertificateException("Unable to load SHA-256 digest and verify pinned certificate");
                }

                // Verify against pinned certificate hash
                if (!Arrays.equals(SERVER_PINNED_HASH, serverHash)) {
                    throw new CertificateException("The host's certificate has not been pinned by this application");
                }
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                // We could validate client certs here, but it is out of scope for what we want to do
                throw new CertificateException("This trust manager does not verify client certificates");
            }
        };

        // Obtain the default socket factory
        SSLContext context = null;
        SSLSocketFactory f = null;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { pinningX509TrustManager }, null);
            f = context.getSocketFactory();
        } catch (Exception e) {
            System.err.println("Error establishing SSL context and factory");
            System.err.println(e.toString());
            System.exit(-1);
        }

        try {
            // Create a socket - will not connect yet
            SSLSocket socket = (SSLSocket) f.createSocket(REMOTE_HOST, REMOTE_PORT);

            // Handshake to create a session
            socket.startHandshake();

            // What parameters were established?
            System.out.println(String.format("Negotiated Session: %s", socket.getSession().getProtocol()));
            System.out.println(String.format("Cipher Suite: %s", socket.getSession().getCipherSuite()));

            // We're reading and writing objects this time
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            // Obtain bank customer from DB
            output.writeInt(100124796);
            output.flush();

            // Read the BankCustomer returned by the server.
            System.out.println("Server Response:");
            BankCustomer customer = (BankCustomer) input.readObject();

            System.out.println(customer);

            input.close();
            output.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e.toString());
        }
    }
}