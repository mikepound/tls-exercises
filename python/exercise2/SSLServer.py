import socket
import ssl
import threading
from pathlib import Path
import select

LOCAL_HOST = 'localhost'
LOCAL_PORT = 8383
RESOURCE_DIRECTORY = Path(__file__).resolve().parent.parent / 'resources' / 'server'
SERVER_CERT_CHAIN = RESOURCE_DIRECTORY / 'server.intermediate.chain.pem'
SERVER_KEY = RESOURCE_DIRECTORY / 'server.key.pem'

class SSLServer:
    """
    Exercise 2 Server
    """
    def __init__(self):
        """
        Creates an SSLContext which provides parameters for any future SSL connections
        """
        context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
        context.load_cert_chain(certfile=SERVER_CERT_CHAIN, keyfile=SERVER_KEY)

        # Part 1: Server
        # Add code here to request mutual authentication from the client. You'll also need to add a reference to the
        # CA cert above
        #
        # For help check out:
        #      https://github.com/mikepound/tls-exercises/blob/master/python/README.md

        self.context = context

    def start_server(self):
        """
        Begins listening on a socket. Any connections that arrive are wrapped in an SSLSocket using the context
        created during initialisation.

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

            # This client and server are sending encoded UTF-8 strings
            print("Received from client:", client_request.decode("UTF-8").rstrip())

            # Reply to the client's request with a small web page.
            self.conn.send("HTTP/1.0 200 OK\nContent-type: text/html\n"
                           "<html>\n"
                           "  <head>\n"
                           "    <title>Tiny Website</title>\n"
                           "  </head>\n"
                           "  <body>\n"
                           "    A very small website!\n"
                           "  </body>\n"
                           "</html>\n".encode("UTF-8"))

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
