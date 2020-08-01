import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.concurrent.TimeUnit;


public class SocksProxyWorker implements Runnable{

    Socket client_socket = null;
    InputStream client_in = null;
    OutputStream client_out = null;

    Socket remote_socket = null;
    InputStream remote_in = null;
    OutputStream remote_out = null;

    Pipe pipe1 = null;
    Pipe pipe2 = null;
    Thread pipe_thread1 = null;
    Thread pipe_thread2 = null;

    public SocksProxyWorker(Socket client_socket) {
        this.client_socket = client_socket;
    }

    /**
     Runnable interface.
    */
    public void run() {
        if (!init_client_streams()){
            print_err_msg("Can't get client socket streams.");
            return;
        }
        // Read first message from client.
        Socks4ClientMessage client_msg = read_client_msg();
        if (client_msg == null){
            send_msg_to_client(client_msg.REPLY_REJECTED);
            abort();
            return;
        }
        // First message should be connect request.
        if (client_msg.command != client_msg.REQUEST_CONNECT)
        {
            print_err_msg("First message from client is not 'CONNECT'");
            send_msg_to_client(client_msg.ip_addr, client_msg.port, client_msg.REPLY_REJECTED);
            abort();
            return;
        }
        if (!connect_to_remote_server(client_msg.ip_addr, client_msg.port)){
            send_msg_to_client(client_msg.ip_addr, client_msg.port, client_msg.REPLY_REJECTED);
            abort();
            return;
        }
        // Connect the client to the wanted remote server and send OK.
        connect_client_to_remote();
        if (!send_msg_to_client(client_msg.ip_addr, client_msg.port, client_msg.REPLY_OK)){
            abort();
            return;
        }

        wait_for_connection_termination();
        abort();
        System.out.println("Closing connection from " +
            client_socket.getInetAddress().getHostAddress() + ":" + client_socket.getPort() +
            " to " +
            remote_socket.getInetAddress().getHostAddress() + ":" + remote_socket.getPort());
    }

    private boolean init_client_streams(){
        try {
            // Set timeout to 30 sec.
            client_socket.setSoTimeout(30000);
            client_in = client_socket.getInputStream();
            client_out = client_socket.getOutputStream();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    private boolean connect_to_remote_server(InetAddress ip_addr, int port){
        try{
            remote_socket = new Socket(ip_addr, port);
            // Set timeout to 10 sec.
            remote_socket.setSoTimeout(10000);
            remote_in = remote_socket.getInputStream();
            remote_out = remote_socket.getOutputStream();
        } catch (IOException ex) {
            print_err_msg("Can't connect to remote server - " +
                ip_addr.getHostAddress() + ":" + port);
            return false;
        }
        return true;
    }

    /**
     Connect the the client socket and the remote server socket.
     Spawn a thread for in-out client to server.
     Spawn a thread for in-out server to client.
    */
    private boolean connect_client_to_remote(){
        pipe1 = new Pipe(client_in, remote_out, remote_socket.getPort());
        pipe2 = new Pipe(remote_in, client_out, 0);
        pipe_thread1 = new Thread(pipe1);
        pipe_thread2 = new Thread(pipe2);
        pipe_thread1.start();
        pipe_thread2.start();
        System.out.println("Successful connection from " +
            client_socket.getInetAddress().getHostAddress() + ":" + client_socket.getPort() +
            " to " +
            remote_socket.getInetAddress().getHostAddress() + ":" + remote_socket.getPort());
        return true;
    }

    /**
     Read a Socks version 4 client message.
    */
    private Socks4ClientMessage read_client_msg(){
        Socks4ClientMessage client_msg = new Socks4ClientMessage();
        DataInputStream d_in = null;
        byte[] addr = null;

        try {
            d_in = new DataInputStream(client_in);
            addr = new byte[4];
            client_msg.version= d_in.readUnsignedByte();
            if (client_msg.version != 4){
                print_err_msg("Got wrong version - " + client_msg.version);
                return null;
            }
            client_msg.command = d_in.readUnsignedByte();
            // Read port.
            client_msg.port = 0;
            client_msg.port |= d_in.readUnsignedByte();
            client_msg.port = client_msg.port << 8;
            client_msg.port |= d_in.readUnsignedByte();
            d_in.readFully(addr);
            client_msg.ip_addr = InetAddress.getByAddress(addr);
            // Wait for null byte.
            int b = 0;
            while ((b = client_in.read()) != 0){

            }
        } catch (IOException ex) {
            print_err_msg("Can't read client message.");
            return null;
        }
        return client_msg;
    }

    /**
     Sent a Socks version 4 server message.
    */
    private boolean send_msg_to_client(InetAddress ip_addr, int port, int return_code){
        byte [] out_msg = new byte[8];
        // Version should be 0.
        out_msg[0] = 0;
        out_msg[1] = (byte)return_code;
        out_msg[2] = (byte) port;
        port = port >> 8;
        out_msg[3] = (byte) port;
        byte[] remote_addr = ip_addr.getAddress();
        System.arraycopy(remote_addr,0,out_msg,4,4);
        try{
            client_out.write(out_msg);
            client_out.flush();
        } catch (IOException ex) {
            print_err_msg("Can't send message back to client");
            return false;
        }
        return true;
    }

    /**
     Sent a Socks version 4 server message when the report and ip address
     are not known.
    */
    private boolean send_msg_to_client(int return_code){
        byte [] out_msg = new byte[8];
        // Version should be 0 for return message.
        out_msg[0] = 0;
        out_msg[1] = (byte)return_code;
        try{
            client_out.write(out_msg);
            client_out.flush();
        } catch (IOException ex) {
            print_err_msg("Can't send message back to client");
            return false;
        }
        return true;
    }

    /**
     Wait for one of the threads to finish.
    */
    private void wait_for_connection_termination(){
        while (true){
            if (!pipe_thread1.isAlive()){
                break;
            }
            if (!pipe_thread2.isAlive()){
                break;
            }
            try{
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    /**
     Close all sockets, streams and and join the threads.
    */
    private void abort(){
        try{
            client_in.close();
            client_out.close();
            client_socket.close();
            if(remote_in != null) remote_in.close();
            if(remote_out != null) remote_out.close();
            if(remote_socket != null) remote_socket.close();
        } catch (IOException ex) {
            print_err_msg("Can't close sockets - " + ex);
        }
        try{
            if(pipe_thread1 != null) pipe_thread1.join();
            if(pipe_thread2 != null) pipe_thread2.join();
        } catch (InterruptedException ex) {
        }
    }

    private void print_err_msg(String err){
        System.err.println("Connection Error: " + err);
    }
}