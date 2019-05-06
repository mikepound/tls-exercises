import socket
import ssl
import threading
from pathlib import Path
import select
from datatypes import BankCustomer
import pickle

LOCAL_HOST = 'localhost'
LOCAL_PORT = 8484
RESOURCE_DIRECTORY = Path(__file__).resolve().parent.parent / 'resources' / 'server'
SERVER_CERT_CHAIN = RESOURCE_DIRECTORY / 'server.intermediate.alt.chain.pem'
SERVER_KEY = RESOURCE_DIRECTORY / 'server.alt.key.pem'

# Part 2: Server
# In this exercise the server requires no additional code.
#
# When testing, you can alternate which set of certificates you use by running either this imposter
# server, or the official one.
#
# For help check out:
#      https://github.com/mikepound/tls-exercises/blob/master/python/README.md

class SSLServer:
    """
    Exercise 3 "Imposter" Server
    """
    def __init__(self):
        context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
        context.load_cert_chain(certfile=SERVER_CERT_CHAIN, keyfile=SERVER_KEY)
        self.context = context

    def start_server(self):
        server_socket = socket.socket()
        server_socket.bind((LOCAL_HOST, LOCAL_PORT))
        server_socket.listen(5)
        read_list = [server_socket]

        print("Listening on port {0}...".format(LOCAL_PORT))

        while True:
            readable, writable, errored = select.select(read_list, [], [], 2)
            for s in readable:
                if s is server_socket:
                    client_socket, address = server_socket.accept()
                    try:
                        # Wrap the socket in an SSL connection (will perform a handshake)
                        conn = self.context.wrap_socket(client_socket, server_side=True)
                        ClientHandler(conn).start()
                    except ssl.SSLError as e:
                        print(e)


class ClientHandler(threading.Thread):
    """
    Thread handler leaves the main thread free to handle any other incoming connections
    """
    def __init__(self, conn):
        threading.Thread.__init__(self)
        self.conn = conn

    def run(self):
        try:
            # Read up to 1024 bytes from the client
            client_request = self.conn.recv(1024)

            # Parse an int containing a bank customer id
            requested_id = int.from_bytes(client_request, 'big')
            print("Received from client:", requested_id)

            # Send back a response - this is just another bank customer but theoretically
            # if receiving trusted data from an imposter, this could be malformed or malicous
            example_customer = BankCustomer(requested_id,
                                            "Imposter data",
                                            "not.real@email.com",
                                            123456789,
                                            654321,
                                            "Nowhere",
                                            "",
                                            "",
                                            "")

            # We need to send bytes, python's pickling makes this simple
            self.conn.send(pickle.dumps(example_customer))

        except ssl.SSLError as e:
            print(e)
        except Exception as e:
            print(e)
        finally:
            self.conn.close()


def main():
    server = SSLServer()
    server.start_server()


if __name__ == '__main__':
    main()
