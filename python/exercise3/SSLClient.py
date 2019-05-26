import socket
import ssl
from pathlib import Path
import pickle
import hashlib


LOCAL_HOST = 'localhost'
LOCAL_PORT = 8484
RESOURCE_DIRECTORY = Path(__file__).resolve().parent.parent / 'resources' / 'client'
CA_CERT = RESOURCE_DIRECTORY / 'ca.cert.pem'
PINNED_FILE = RESOURCE_DIRECTORY / 'pinned.hash'


def main():
    """
    Exercise 3 Client
    """
    # Create a standard TCP Socket
    sock = socket.socket(socket.AF_INET)

    # Create SSL context which holds the parameters for any sessions
    context = ssl.create_default_context(ssl.Purpose.SERVER_AUTH)
    context.check_hostname = False
    context.load_verify_locations(CA_CERT)

    # We can wrap in an SSL context first, then connect
    conn = context.wrap_socket(sock, server_hostname="Expert TLS Server")
    try:
        # Handshake - conn is an SSLSocket
        conn.connect((LOCAL_HOST, LOCAL_PORT))

        # Part 1: Client
        # Both the regular and "imposter" servers have certificates signed by the CA. This means the
        # best way to limit connections to a *single* server is to pin its certificate.
        #
        # Here you need to add hashing validation against the received certificate, comparing it to
        # the hash you have on file in 'pinned.hash'.
        #
        # For help check out:
        #      https://github.com/mikepound/tls-exercises/blob/master/python/README.md

        # What parameters were established?
        print("Negotiated session using cipher suite: {0}\n".format(conn.cipher()[0]))

        # We are sending a 4 byte int, convert into bytes
        conn.send((100124796).to_bytes(4, 'big'))

        # Receive a number back from the server
        server_response = conn.recv(1024)

        # Unpickle the bytes from the server into a datatypes.BankCustomer
        customer_returned = pickle.loads(server_response)
        print(customer_returned)

    except ssl.CertificateError:
        print("The host's certificate has not been pinned by this application")
    finally:
        conn.close()


if __name__ == '__main__':
    main()
