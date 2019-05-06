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
    conn = context.wrap_socket(sock, server_hostname=LOCAL_HOST)
    try:
        # Handshake - conn is an SSLSocket
        conn.connect((LOCAL_HOST, LOCAL_PORT))

        # Verify against pinned hash
        dat = conn.getpeercert(binary_form=True)
        hashalg = hashlib.sha256()
        hashalg.update(dat)
        cert_hash = hashalg.digest()

        with open(PINNED_FILE, 'rb') as pinned:
            pinned_hash = pinned.read()

        if cert_hash != pinned_hash:
            raise ssl.CertificateError("Non-pinned certificate!")

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
