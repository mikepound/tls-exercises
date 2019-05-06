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
    # Create a standard TCP Socket
    sock = socket.socket(socket.AF_INET)

    # Create SSL context which holds the parameters for any sessions
    context = ssl.create_default_context(ssl.Purpose.SERVER_AUTH)
    context.check_hostname = False
    context.load_verify_locations(CA_CERT)
    
    # Restrict available protocols and ciphers
    context.options |= ssl.OP_NO_TLSv1 | ssl.OP_NO_TLSv1_1 | ssl.OP_NO_SSLv3
    context.set_ciphers('ALL:!DSS:!DHE:!aNULL:!eNull')

    # We can wrap in an SSL context first, then connect
    conn = context.wrap_socket(sock, server_hostname=LOCAL_HOST)
    try:
        # Handshake - conn is an SSLSocket
        conn.connect((LOCAL_HOST, LOCAL_PORT))

        # What parameters were established?
        print("Negotiated session using cipher suite: {0}\n".format(conn.cipher()[0]))

        # In python sockets send and receive bytes. Send some numbers:
        conn.send(bytes([2, 3, 5, 7, 11, 13, 17, 19, 23]))

        # Receive a number back from the server
        server_response = conn.recv(1024)

        # Server response is an int, convert it back
        print(int.from_bytes(server_response, 'big'))
    finally:
        conn.close()


if __name__ == '__main__':
    main()
