import java.net.*;


/**
    Client message structure.
*/
class Socks4ClientMessage
{
    public InetAddress ip_addr;
    public int port;
    public int version;
    public int command;

    static final int SOCKS_VERSION              = 4;
    public final static int REQUEST_CONNECT     = 1;

    public final static int REPLY_OK            = 90;
    public final static int REPLY_REJECTED      = 91;
};
