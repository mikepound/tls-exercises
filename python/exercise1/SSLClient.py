import socket
import ssl
from pathlib import Path


LOCAL_HOST = 'localhost'
LOCAL_PORT = 8282
RESOURCE_DIRECTORY = Path(__file__).resolve().parent.parent / 'resources' / 'client'
CA_CERT = RESOURCE_DIRECTORY / 'ca.cert.pem'


def main():
    """
    Exercise 1 Client
    """

    # Part 1
    # Add code here to create and configure an SSLContext, wrap a socket and connect using TLS
    # For help check out:
    #      https://github.com/mikepound/tls-exercises/blob/master/python/README.md

    # Create a standard TCP Socket
    sock = None

    # Create SSL context which holds the parameters for any sessions
    context = None

    # We can wrap in an SSL context first, then connect
    conn = None
    try:
        # Connect using conn

        # The code below is complete, it will use a connection to send and receive from the server

        if conn is None:
            return

        # What parameters were established?
        print("Negotiated session using cipher suite: {0}\n".format(conn.cipher()[0]))

        # In python sockets send and receive bytes. Send some numbers:
        conn.send(bytes([2, 3, 5, 7, 11, 13, 17, 19, 23]))

        # Receive a number back from the server
        server_response = conn.recv(1024)

        # Server response is an int, convert it back
        print(int.from_bytes(server_response, 'big'))
    finally:
        if conn is not None:
            conn.close()


if __name__ == '__main__':
    main()
