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

Notice that the trust store only contains the root certificate, while the key store contains the chain up to the root.

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

# Java Keystores
This section is not required for the exercises, but it is helpful to know a few of the java keytool commands, for using and manipulating keystores. A keystore is a java object that holds any number of certificates and/or private keys. In the exercises they are used on both the client and server sides, to store the certificates and keys required.

You can create keystores using the `keytool` command, and you can also inspect and manipulate the contents of the main java keystore that is shipped with the JRE.

## Listing keystore contents
The following command will list the contents of a keystore:

```
keytool -list -keystore ClientKeyStore.jks

Enter Keystore Password: experttls

Your keystore contains 2 entries

client, 16 May 2019, PrivateKeyEntry,
Certificate fingerprint (SHA-256): 4B:23:44:D2:4E:26:FE:D8:AC:D5:22:77:D0:B2:3A:3F:BA:91:C3:0F:C7:3B:BA:24:65:C6:15:31:78:AE:79:3C
intca, 24 Oct 2019, trustedCertEntry,
Certificate fingerprint (SHA-256): C8:A7:B4:76:CD:52:F0:DB:16:00:E2:EA:3B:49:33:93:07:69:2F:0E:6B:B8:BF:FE:E3:11:8E:CE:27:3B:B1:97
```
In this case we're examining the client key store, which is found in the resources directory of the exercises. It contains two things, the client certificate / key (a PrivateKeyEntry), and the int ca certificate (a trustedCertEntry). These are supplied when the client authenticates during Exercise 2. The client trust store only contains the root ca certificate.

## Adding into an existing keystore
You can import an existing certificate into a keystore like this:
```
keytool -importcert -keystore ClientTrustStore.jks -alias rootca -file ca.cert.cer

Enter keystore password: experttls

Owner: CN=Expert TLS Root CA, OU=IT Training, O=Expert TLS, ST=England, C=GB
Issuer: CN=Expert TLS Root CA, OU=IT Training, O=Expert TLS, ST=England, C=GB
Serial number: 31cc7897c7cb8518c59592400ae69c2e41fc8dd5
Valid from: Sat Apr 27 18:32:21 BST 2019 until: Mon Sep 03 18:32:21 BST 2040
Certificate fingerprints:
         SHA1: 3B:55:2D:54:68:02:AC:61:83:4C:02:29:A4:90:C9:00:5D:6E:F7:BF
         SHA256: 13:EC:ED:75:D7:AB:A9:EE:8C:B8:AC:E8:56:40:9B:86:01:7E:83:47:4A:DB:2A:37:7A:3D:10:ED:26:EA:16:CA
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 4096-bit RSA key
Version: 3

Extensions:

...

Trust this certificate? [no]: yes
Certificate was added to keystore
```
If a certificate you are adding is signed by a trusted certificate in the keystore, it will be automatically added. If not, you will need to type yes to confirm you trust this certificate.

### Importing an OpenSSL Certificate and Private Key
This is surprisingly difficult! In some cases you would generate a key pair within the keystore itself using `keytool -genkeypair`, then you can generate a certificate signing request using `keytool -gencert`. This allows you to then have OpenSSL or some CA sign a certificate, which can then be imported.

During these exercises, all key pairs and certificates were generated using OpenSSL. Keytool doesn't accept pem files, we have to convert via a pkcs12 file. First, convert the certificate and key into pkcs12 format:
```
openssl pkcs12 -export -in server.cert.pem -inkey server.key.pem
    -name "server" -out server.p12

Enter pass phrase for server.key.pem: experttls
Enter Export Password: newpassword
Verifying - Enter Export Password: newpassword
```
You absolutely must set a password here, even if you intend to delete this file. A bug means keytool won't accept pkcs12 files that are not password protected. Next, create a keystore based on this file:
```
keytool -importkeystore -deststorepass experttls -destkeypass experttls
    -destkeystore ServerKeyStore.keystore -srckeystore server.p12
    -srcstoretype PKCS12 -srcstorepass newpassword -alias servercert
```
This will create a new key store that contains both the server's certificate, and its private key.
