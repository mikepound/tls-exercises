# OpenSSL Certificate Authority Example

This is an example CA set up to serve certificates to the servers and clients in use within these exercises. There is currently one root certificate, one intermediate certificate, and then a handful of end entity certificates.

## Introduction
#### Directory Structure
The root directory of the CA contains a number of directories:

| Directory        | Description                                   |
| ---------------- | --------------------------------------------- |
| ca/certs/        | Any signed certificates with requested names  |
| ca/newcerts/     | Signed certificates named by serial number    |
| ca/private/      | Private key files                             |
| ca/intermediate/ | The intermediate CA and its files             |

The `ca/private/` directory contains the most important part of the CA, the root certificates private key. Real CAs take extreme measures to protect this key!

Within the CA directory are a number of files associated with the CA. A config file `openssl.cfg` provides configuration for OpenSSL. This includes default values, and extension information for things like key usages. `index.txt` and `serial` hold a list of previously signed keys, which are stored in `ca/newcerts`.

The intermediate CA has a similar structure:

| Directory                 | Description                                   |
| ------------------------- | --------------------------------------------- |
| ca/intermediate/certs/    | Any signed certificates with requested names  |
| ca/intermediate/newcerts/ | Signed certificates named by serial number    |
| ca/intermediate/private/  | Private key files                             |
| ca/intermediate/csr/      | Certificate signing requests for this int CA  |

#### The Signing Process

Whenever a new certificate needs to be signed, there is a process:

1) Generate a new public key / private key pair and an associated certificate (usually stored as .pem files)
2) Generate a certificate signing request (CSR)
3) Provide the CA with the above, at which point they will sign the certificate with their private key

#### Root and Intermediate Certificates
All CAs have at least one root key pair. The private keys of these are so protected that they are used rarely, often stored on machine without a network connection, in a secured room. Typically `root certificates` have a long lifespan, and are used to sign a handful of `intermediate certificates`. These are used to perform actual day to day signing activities, usually through automated systems. Using intermediate certificates allows us to revoke them if they are compromised. It's simply a form of delegation, and means the highly protected root keys can be kept safe unused.

## Setting up the CA
#### The Root Certificate
The first task is to generate the root key pair:

```
openssl genrsa -aes256 -out private/ca.key.pem 4096

Enter pass phrase for ca.key.pem: experttls
Verifying - Enter pass phrase for ca.key.pem: experttls
```

We then create a root certificate, signed using the certificates own private key. All root certificates are signed this way, also note that a CSR isn't required for the root certificate.

```
openssl req -config openssl.cfg -key private/ca.key.pem
    -new -x509 -days 7300 -sha256 -extensions v3_ca -out certs/ca.cert.pem

Enter pass phrase for ca.key.pem: experttls

...

Country Name (2 letter code) [GB]:
State or Province Name [England]:
Locality Name []:
Organization Name [Expert TLS]:
Organizational Unit Name [IT Training]:
Common Name []:Expert TLS Root CA
Email Address []:
```
#### The Intermediate Certificate
We begin by creating an intermediate key pair within the `ca/intermediate/` directory:

```
openssl genrsa -aes256 -out intermediate/private/intermediate.key.pem 4096

Enter pass phrase for intermediate.key.pem: experttls
Verifying - Enter pass phrase for intermediate.key.pem: experttls
```

The certificate for this key pair will need to be signed by the root certificate. To do this we generate a certificate signing request using the `openssl req` command:

```
openssl req -config intermediate/openssl.cfg -new -sha256
    -key intermediate/private/intermediate.key.pem -out intermediate/csr/intermediate.csr.pem

Enter pass phrase for intermediate.key.pem: experttls

...

Country Name (2 letter code) [GB]:
State or Province Name [England]:
Locality Name []:
Organization Name [Expert TLS]:
Organizational Unit Name [IT Training]:
Common Name []:Expert TLS Int CA
Email Address []:
```

This creates the certificate, which we sign with the root CA key using the `openssl ca` command:

```
openssl ca -config openssl.cnf -extensions v3_intermediate_ca -days 3650 -notext -md sha256
    -in intermediate/csr/intermediate.csr.pem
    -out intermediate/certs/intermediate.cert.pem

Enter pass phrase for ca.key.pem: experttls
Sign the certificate? [y/n]: y
```

This creates a certificate with the a serial from `serial` and adds a record into `index.txt`. Now the CA is ready to go! You would generally now use this intermediate certificate, or another one, to sign any other end entity certificates.

## Using the CA
Signing incoming CSRs is similar to the process above. In this case since we're generating our own certificates for use in TLS sessions, we'll generate another private key for our TLS server. For standard TLS servers, 2048 bit RSA is adequate, and also a little faster than 4096 bits when performing handshakes.

#### Generate the key

```
openssl genrsa -aes256 -out intermediate/private/server.key.pem 2048

Enter pass phrase for server.key.pem: experttls
Verifying - Enter pass phrase for server.key.pem: experttls
```
It perhaps goes without saying you should use different passwords for all of these keys! Next we generate the CSR:

```
openssl req -config intermediate/openssl.cfg
    -key intermediate/private/server.key.pem
    -new -sha256 -out intermediate/csr/server.csr.pem

Enter pass phrase for server.key.pem: experttls

...

Country Name (2 letter code) [GB]:
State or Province Name [England]:
Locality Name []:
Organization Name [Expert TLS]:
Organizational Unit Name [IT Training]:
Common Name []:Expert TLS Server
Email Address []:
```

If you were hosting a website, it's important that your common name (CN) matches the domain name you'll be using. Browsers and other clients typically check against the hostname when verifying certificates. In this case, we're using these certificates internally, so I've chosen to use more semantically meaningful common names.

Finally, we can sign using the intermediate certificate (not the root!):
```
openssl ca -config intermediate/openssl.cfg -extensions server_cert -days 375 -notext
    -md sha256 -in intermediate/csr/server.csr.pem 
      -out intermediate/certs/server.cert.pem
```

## Other Useful Commands
OpenSSL has a great many features, here are some useful ones:

Examine a certificate

```
openssl x509 -noout -text -in server.cert.pem
```

Copy a certificate to a different format (e.g. DER)
```
openssl x509 -in server.cert.pem -out server.cert.cer
```

Copy a certificate and key into a pkcs12 file. Useful for a few things, notably importing into a java keystore
```
openssl pkcs12 -export -in server.cert.pem -inkey server.key.pem
    -name "server" -out server.p12
```

Verify a certificate chain
```
openssl verify -CAfile ca-chain.cert.pem server.cert.pem

server.cert.pem: OK
```

Note that the ca-chain file will contain both the root and intermediate keys. Or any keys necessary to complete the chain. This can help you verify the chain is valid between the server certificate and those certificates in the rest of the chain. If you have a trusted root certificate, you can use this command to verify a chain:

```
openssl verify -CAfile ca.cert.pem -untrusted int.cert.pem server.cert.pem
```

## Ed25519 Certificates
Edward's curve certificates use a form of elliptic-curve cryptography. Signatures based on them are modern, efficient, and secure. There is one Ed25519 server certificate signed in the repository. It's not used during the exercises, it's purely here as an example. They make a good option as long as the connecting clients support them. These are the commands I used to generate the certificate:

Generate a key pair
```
openssl genpkey -algorithm ed25519 -aes256
    -out intermediate/private/server.ed25519.key.pem
```

Create a CSR
```
openssl req -config intermediate/openssl.cfg
    -key intermediate/private/server.ed25519.key.pem
    -new -sha256 -out intermediate/csr/server.ed25519.csr.pem
```

Sign using the intermediate CA
```
openssl ca -config intermediate/openssl.cfg -extensions server_cert -days 375
    -notext -md sha256 -in intermediate/csr/server.csr.pem
    -out intermediate/certs/server.cert.pem
```

Create a DER encoded version of the certificate too
```
openssl x509 -in intermediate\certs\server.ed25519.cert.pem
    -out intermediate\certs\server.ed25519.cert.der
```

## Testing a Server Handshake
It can be tricky to work out what's going wrong in a handshake, whether it's a certificate problem or a problem with your server or client setup. OpenSSL has a test client that is really useful in this case. The `openssl s_client` command lets us connect to a server and view detailed debugging information.
```
openssl s_client -CAfile ca.cert.pem localhost:8282
```
This command will connect to a server using a root certificate, and verify a successful handshake. Once connected, you can send and receive data from the command line.

```
openssl s_client -debug -CAfile ca.cert.pem localhost:8282
```
The `-debug` flag produces significantly more output information.

```
openssl s_client -tls1_3 -CAfile ca.cert.pem localhost:8282
```
You can add additional parameters such as `-tls1_3` for negotiate specific protocols and ciphers, for testing.

```
openssl s_client -tlsextdebug –state -CAfile ca.cert.pem localhost:8282
```
The `-tlsextdebug` flag produces more information on extensions in use.
