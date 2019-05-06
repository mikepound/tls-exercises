import socket
import ssl
import threading
from pathlib import Path
import select

LOCAL_HOST = 'localhost'
LOCAL_PORT = 8282
RESOURCE_DIRECTORY = Path(__file__).resolve().parent.parent / 'resources' / 'server'
SERVER_CERT_CHAIN = RESOURCE_DIRECTORY / 'server.intermediate.chain.pem'
SERVER_KEY = RESOURCE_DIRECTORY / 'server.key.pem'


class SSLServer:
    """
    Exercise 1 Server
    """
    def __init__(self):
        """
        Creates an SSLContext which provides parameters for any future SSL connections
        """
        context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
        context.load_cert_chain(certfile=SERVER_CERT_CHAIN, keyfile=SERVER_KEY)
        context.options |= ssl.OP_NO_TLSv1 | ssl.OP_NO_TLSv1_1 | ssl.OP_NO_SSLv3
        context.set_ciphers('ALL:!DSS:!DHE:!aNULL:!eNull')
        self.context = context

    def start_server(self):
        """
        Begins listening on a socket. Any connections that arrive are wrapped in an SSLSocket using the context
        created during initialisation

        Makes use of the OS select function to perform basic non-blocking IO. Once a connection has established
        an instance of ClientHandler is created to serve the client
        """
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
            print("Received from client:", client_request)

            # Return the product of the client's byte values
            product = 1
            for n in client_request:
                product *= n

            # You are responsible for converting any data into bytes strings
            self.conn.send(product.to_bytes(4, 'big'))
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
