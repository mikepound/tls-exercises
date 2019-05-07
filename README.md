# SSL/TLS Exercises

These exercises are associated with my course on Transport Layer Security (TLS). This course is delivered via O'Reilly Live Training. When not teaching online I am a lecturer at the University of Nottingham, and appear on YouTube.

- [O’Reilly live training](https://www.oreilly.com/live-training/courses/expert-transport-layer-security-tls/0636920282907)
- [My university page](https://www.nottingham.ac.uk/research/groups/cvl/people/michael.pound)
- [Computerphile](https://youtube.com/computerphile)

## Introduction
Exercises are available in either Python or Java, depending on which language you are most familiar with. Although the languages and their support for TLS are quite different, the exercises are identical - at least in general structure and the goal. In each case you need to complete any code necessary to establish the required TLS connection between a client and server. If you're familiar with the language, each exercise should not take too long.

**Note:** The focus of these exercises is an introduction in TLS, not a lesson in production ready code! With this in mind, I've kept unnecessary structure and error correcting code to a minimum. It's quite possible to produce exceptions, use these as a guide to find out what requires fixing.

## The Exercises
There are three exercises involving different client-server communications. In each case you have some or all of both the client and server implementations, and you must either fix or add to the existing code.

**Exercise 1:** You have a working server, and some of a client implementation. You need to add the remaining SSL code necessary to get the two programs communicating. Once this is done, you should add some code to ensure only the most recent ciphers and protocols are negotiated between the parties.

**Exercise 2:** You have working client and server implementations. You need to alter both in order to create a mutually authenticated session. That is, one in which the client is also authenticated using a certificate

**Exercise 3:** You have a working server and client, but a second "imposter" server exists that also has a valid certificate from the CA. You need to implement pinning on the original certificate in order to verify which server is the real one.

## Materials
The repository contains a number of subfolders, most contain their own readme file with more detailed information.
### Python
Exercises in python using the standard ssl library. These exercises have been written to work in Python 3.6 and 3.7.
- [Exercises](./python/)
- [Answers](answers/python/)

### Java
Exercises in Java using the javax.net and javax.net.ssl packages. These exercises were compiled using a modern Java SDK targeting Java 8.
- [Exercises](./java/)
- [Answers](answers/java/)

### OpenSSL CA
An example certificate authority created using [OpenSSL](https://www.openssl.org/) 1.1.1b. This CA was used to generate the keys and signed certificates found in the exercises. If you wish to manage your own certificate chains, you can use this folder as an example. Instructions on the use of this CA are provided in the readme, but bear in mind that correct certificate management is extremely important, be sure to read up!
