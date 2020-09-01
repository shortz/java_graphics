Files description:
Sockspy.java - The main server - contains the server socket, the thread pool and
the main function.

SocksProxyWorker.java - Manage the client connection to the remote server.
Open two threads one for client-to-server and one for server-to-client.

Pipe.java - Connect two streams together - and search for the HTTP connection.

Socks4ClientMessage.java - Client message structure.


How to run the server:
javac -cp . -d . Sockspy.java Pipe.java SocksProxyWorker.java Socks4ClientMessages.java
java Sockspy
