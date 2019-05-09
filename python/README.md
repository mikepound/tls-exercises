# Python Exercises
These exercises have been written and tested in Python 3.6 and 3.7. Support for TLS differs between versions of Python, I would recommend using the most recent version you can. If you need reliable TLS connections in an older version of python, you might consider the [pyOpenSSL](https://github.com/pyca/pyopenssl) project for TLS, and [Cryptography](https://github.com/pyca/cryptography) for X509 certificate management. These exercises can be run from the command line, but I used [PyCharm](https://www.jetbrains.com/pycharm/).

## Exercise 1: Setting up a TLS Connection
In this exercise you have a completely working server. It will negotiate TLS sessions and the communicate with clients. Once set up, the server simply recieves lists of bytes, and then returns the product of all of the numbers received. E.g. [2,5,6] -> 60.

The client is less complete, at the start of the exercise it has code missing, so it won't connect to a server, and without an available socket, it will simply exit.

#### Part 1: Connecting with the client
The client doesn't have a working socket implementation. To begin you need to create a new `socket.socket`, create an `SSLContext` with appropriate configuration, and then use this to wrap the socket into an `SSLSocket`.

While you work, it'd be useful to have the [socket](https://docs.python.org/3.6/library/socket.html) and [ssl](https://docs.python.org/3.6/library/ssl.html) documentation. You can now either get stuck in using the existing code and documentation, or follow along below for a little more advice.

1) Begin by creating a new socket using the `socket.socket()` function. Make sure it's of the `AF_INET` type.
2) Obtain the default ssl context via the `ssl.create_default_context()` function. The purpose for this one is to authenticate servers: `ssl.Purpose.SERVER_AUTH`.
3) You'll want to set `check_hostname` to false, as our hostname is meaningless in this exercise.
4) Continuing with the context, load the CA certificate via the `context.load_verify_locations()` function. This ensures that the client knows which root certificate to validate against.
5) Now we need a connection. Wrap the original socket into an SSLSocket using `context.wrap_socket()` (see [documentation](https://docs.python.org/3.6/library/ssl.html#ssl.SSLContext.wrap_socket)). We're calling this socket `conn`.
6) Inside the try-catch block, make use of the sockets `connect()` function, which will expect a tuple containing the hostname and port defined at the top of the file.

Don't forget to start the server, too! With any luck, this should now connect nicely.

#### Part 2: Specifying protocols and cipher suites
TLS works by ordering available cipher suites by preference. You can control what ciphers are used simply by putting the weaker ones at the end. Given you have full control over both the client and server implementations, it's actually easier and safer simply to disable all but the strongest ciphers and protocols.

1) Specify `context.options` to disable everything except TLS 1.3 and 1.2 (see [documentation](https://docs.python.org/3.6/library/ssl.html#ssl.SSLContext.options)). Depending on your build and python version, TLS 1.3 may not be available for you, so be sure to leave 1.2 enabled.
2) Use the `context.set_ciphers()` function to list ciphers you are happy to use. This requires a string in the [OpenSSL cipher list format](https://www.openssl.org/docs/manmaster/man1/ciphers.html). This interface is extremely unintuitive! By way of example:
`'ALL:!DSS:!DHE:!aNULL:!eNull'` will enable all ciphers except those that use the digital signature scheme, non EC diffie-hellman, and any that don't provide encryption or authentication. If you have TLS1.3 enabled, you will find you can't disable these ciphers.

You can make these changes to both the client and the server.

#### Extra credit!
Have a play around with the different cipher suites. You'll find it's quite easy to break your TLS connection when the server and client don't have at least one valid protocol (e.g. TLS1.2) and one cipher suite between them. The client will usually not provide an informative error message, but check the server's console output.

## Exercise 2: Mutual Authentication
In this exercise both the client and server already work, but we are only authenticating the server. You need to add code to both in order to achieve a mutually authenticated session. The client and server here sent a simple HTTP GET request, and an HTML response.

#### Part 1: The Server
The server needs to request a certificate from the client. Make use of the [documentation](https://docs.python.org/3.6/library/ssl.html#ssl.SSLContext).

1) Within the server `__init__` the context is configured. Add code to set the `verify_mode` to `ssl.CERT_REQUIRED`.
2) Provide the server with the root CA certificate to be able to validate the client certificate when it arrives. It's already in the resource folder for the server. Define it as a resource at the top, then use the load_verify_locations() function to provide it.

If you run the server and client now, you'll find the server should reject the client as it doesn't provide a certificate.

#### Part 2: The Server

Setting up the client to provide a certificate is the same code the server already contains to provide its own.

1) The certificates and keys are in the client resource directory. Define `CLIENT_KEY` and `CLIENT_CERT_CHAIN`.
2) Call `context.load_cert_chain()` and provide the new key file and certificate chain.

#### Extra Credit!

If you have extra time, check out the client or servers certificates and keys within the resources directory. If you have OpenSSL installed and available from the command line, try these:
```
openssl rsa -in client.key.pem -noout -text

openssl x509 -in client.intermediate.chain.pem -noout -text
```

## Exercise 3
In this exercise there are two possible servers, one acting as an imposter. Both servers have valid certificates - perhaps a private key got leaked - but in any case we want to configure the client to only accept a single certificate. This is called pinning. The client and servers use this connection to send a fictitious banking record. The pickle module allows us to serialise objects as bytes for transmission over a network.

#### The Client
In this part you might like to use the python `hashlib` [documentation](https://docs.python.org/3/library/hashlib.html#hash-algorithms) if you're not familiar with it. As before the ssl documentation is [here](https://docs.python.org/3.6/library/ssl.html). If you look at the top you'll see I've defined `PINNED_FILE` which is a 32 byte binary file containing a hash of the servers certificate that was calculated ahead of time. We're going to compare these bytes with a hash of the incoming server certificate. 

Python's `ssl` library doesn't let us hook into the TLS validation function, so we'll perform validation immediately after connection, before we send any data.

1) Load the pinned hash of the server's real certificate into a bytes array. This is a standard file `open` in `rb` mode.
2) After `connect()`, use `getpeercert()` to obtain the server's cerificate. Ensure the parameter `binary_form` is true, as this returns a DER encoded certficate, which is what was pinned.
3) Hash the binary certificate and compare this to the hash you loaded. If they don't match, raise an `ssl.CertificateError`!

You'll now find that the imposter server is rejected, but the original server works fine.

#### Extra Credit!

It's quite common to apply a "Trust on First Use" policy for clients. You could do this by pinning the first hash you encounter the first time. Try adapting the client to save a pinned hash the first time a server connects, and then rejecting any different ones after this. You might be familiar with this process from when you pin public keys for servers in SSH.
