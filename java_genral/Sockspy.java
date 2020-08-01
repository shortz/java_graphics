import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Sockspy {
    static Thread       runningThread= null;
    public static final int NUM_OF_THREADS = 20;
    public static final int PORT_NUMBER = 8080;
    public static final int SOCKET_TIMEOUT = 30000;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);

        // Connect server to port PORT_NUMBER.
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException ex) {
            System.err.println("Can't setup server on port " + PORT_NUMBER);
        }
        // Accept each socket and add it the thread pool.
        while (true){
            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(SOCKET_TIMEOUT);
            } catch (IOException ex) {
                System.err.println("Can't accept client connection.");
            }
            threadPool.execute( new SocksProxyWorker(socket));
        }
    }
}