import socket
import ssl
from pathlib import Path


LOCAL_HOST = 'localhost'
LOCAL_PORT = 8383
RESOURCE_DIRECTORY = Path(__file__).resolve().parent.parent / 'resources' / 'client'
CLIENT_CERT_CHAIN = RESOURCE_DIRECTORY / 'client.intermediate.chain.pem'
CLIENT_KEY = RESOURCE_DIRECTORY / 'client.key.pem'
CA_CERT = RESOURCE_DIRECTORY / 'ca.cert.pem'


def main():
    """
    Exercise 2 Client
    """
    # Create a standard TCP Socket
    sock = socket.socket(socket.AF_INET)

    # Create SSL context which holds the parameters for any sessions
    context = ssl.create_default_context(ssl.Purpose.SERVER_AUTH)
    context.load_verify_locations(CA_CERT)
    context.load_cert_chain(certfile=CLIENT_CERT_CHAIN, keyfile=CLIENT_KEY)

    # We can wrap in an SSL context first, then connect
    conn = context.wrap_socket(sock, server_hostname="Expert TLS Server")
    try:
        # Handshake - conn is an SSLSocket
        conn.connect((LOCAL_HOST, LOCAL_PORT))

        # What parameters were established?
        print("Negotiated session using cipher suite: {0}\n".format(conn.cipher()[0]))

        # This time we are sending a string as bytes
        conn.send(b"GET /index.html HTTP/1.1\n")

        # Receive a binary string response
        server_response = conn.recv(1024)

        # Convert the response into a unicode string as that's what the server sent us
        print(server_response.decode("UTF-8"))
    finally:
        conn.close()


if __name__ == '__main__':
    main()
