# Java Exercises
These exercises have been written and tested in Java 8, although in general I would recommend using the most recent version of Java you have available. These exercises can be compiled and run from the command line, but I used [IntelliJ](https://www.jetbrains.com/idea/) with Maven.

## Exercise 1: Setting up a TLS Connection
In this exercise you have a completely working server. It will negotiate TLS sessions and the communicate with clients. Once set up, the server simply recieves lists of bytes, and then returns the product of all of the numbers received. E.g. [2,5,6] -> 60.

The client is less complete, at the start of the exercise it has code missing, so it won't connect to a server, and without an available socket, it will simply return.

#### Part 1: Connecting with the client
The client doesn't have a working socket implementation. To begin you need to create a new `SSLSocketFactory`, then use it to create an `SSLSocket` with an appropriate configuration.

While you work, it'd be useful to have the [SSLSocket](https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLSocket.html) and [SSLSocketFactory](https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLSocketFactory.html) documentation to hand. You can now either get stuck in using the existing code and documentation, or follow along below for a little more advice.

1) Begin by creating a new socket SSLSocketFactory by calling the `getDefault()` static method.
2) You can then call `.createSocket()` on this new factory, passing a port and hostname, which are stored as static variables.
3) You'll need to cast them back to (SSLSocketFactory) and (SSLSocket).
4) On the socket, `.startHandshake()` will negotiated a TLS session with the server.

Test it out, and don't forget to start the server! With any luck, this should now connect nicely.

#### Part 2: Specifying protocols and cipher suites
TLS works by ordering available cipher suites by preference. You can control what ciphers are used simply by putting the weaker ones at the end. Given you have full control over both the client and server implementations, it's actually easier and safer simply to disable all but the strongest ciphers and protocols.

1) The functions your interested in should be run prior to calling `startHandshake()`. First try polling the socket to see what protocols and ciphers are available by default. Use `socket.getEnabledProtocols()` and `.getEnabledCipherSuites()` to see lists of what are enabled.
2) Use `socket.setEnabledProtocols(String[])` to enable only TLS 1.3 and 1.2.
3) Use `socket.setEnabledCipherSuites(String[])` to enable only certain ciphers. For example `TLS_AES_256_GCM_SHA384 or TLS_CHACHA20_POLY1305_SHA256.

You can make these changes to both the client and the server. The server uses an `SSLServerSocket`, but the interface in this case is the same.

#### Extra credit!
Have a play around with the different cipher suites. You'll find it's quite easy to break your TLS connection when the server and client don't have at least one valid protocol (e.g. TLS1.2) and one cipher suite between them.

## Exercise 2: Mutual Authentication
In this exercise both the client and server already work, but we are only authenticating the server. You need to add code to both in order to achieve a mutually authenticated session. The client and server here sent a simple HTTP GET request, and an HTML response.

#### Part 1: The Server
The server needs to request a certificate from the client. Make use of the [documentation](https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLServerSocket.html).

1) Begin by defining two new static variables `TRUSTSTORE_LOCATION` and `TRUSTSTORE_PASSWORD`, the trust store is in the resource directory, so you can use the `class.getResource` method as with the keystore.
2) Within the main method, add the relevant properties for `trustStore` and `trustStorePassword` to ensure that the JVM knows which trust store to use.
3) In the server itself, before the call to accept, use the `socket.setNeedClientAuth()` to request a client certificate.

#### Part 2: The Client

Setting up the client to provide a certificate is the same code the server already contains to provide its own.

1) Set up static variables for `KEYSTORE_LOCATION` and `KEYSTORE_PASSWORD`
2) Add properties for `keystore` and `keystorePassword` within the main method.

#### Extra Credit!

If you have extra time, have a look inside the trust and key stores using `keytool`. Try these:
```
keytool -list -keystore ClientKeyStore.jks

keytool -list -v keystore ClientTrustStore.jks
```

Notice that the trust store only contains the root certificate, while the key store contains the entire chain.

## Exercise 3: Certificate Pinning
In this exercise there are two possible servers, one acting as an imposter. Both servers have valid certificates - perhaps a private key got leaked - but in any case we want to configure the client to only accept a single certificate. This is called pinning. The client and servers use this connection to send a fictitious banking record. This is implemented using a serialisable class, and Object streams.

#### The Client
In this part you might like to use the `java.security.MessageDigest` [documentation](https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html) which also shows a short example. In this exercise we'll also make use of an `SSLContext` ([documentation](https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLContext.html)). Begin by taking a look carefully through the client code. Notice we have loaded a pinned hash from a resource as a static variable. We're also using the `SSLContext` class to supply our own `X509TrustManager` ([documentation](https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/X509TrustManager.html)). We do this so we can create an anonymous class that overrides key parts of the certificate verification process. Find the `checkServerTrusted` function. We're calling `defaultTrustManager.checkServerTrusted()` so the default implementation simply examines the certificate chain as normal. Your task is to add a hash check against the pinned hash.


1) The method is passed the server's certificate chain. Obtain the first entry at `[0]`, which is the server's certificate.
2) Obtain a DER encoded version of this certificate via the .getEncoded() method. Check out the [documentation](https://docs.oracle.com/javase/8/docs/api/java/security/cert/X509Certificate.html).
3) Hash this encoding using a `MessageDigest` using SHA256.
4) Compare this hash to the pinned one, if they do not match, throw a `CertificateException`! You'll likely need to handle the MessageDigest's `NoSuchAlgorithmException` in which case you can also raise a certificate exception.

You'll now find that the imposter server is rejected, but the original server works fine.

#### Extra Credit!

It's quite common to apply a "Trust on First Use" policy for clients. You could do this by pinning the first hash you encounter the first time. Try adapting the client to save a pinned hash the first time a server connects, and then rejecting any different ones after this. You might be familiar with this process from when you pin public keys for servers in SSH.