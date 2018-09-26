package Server.TCP;

import Server.Common.Middleware;
import Server.Interface.IResourceManager;
import java.net.*;

public class TCPMiddleware extends Middleware {
    private static String s_serverName = "Server";
    //TODO: REPLACE 'ALEX' WITH YOUR GROUP NUMBER TO COMPILE
    private static String s_rmiPrefix = "group20";
    private static int s_portNumber;
    public static void main(String args[])
    {
        // Create the TCP server entry
        if (args.length < 7) {
            System.out.println("Usage: java TCPMiddleware middlewareportnum, server1, server1 port, server 2, server2 port, server3 server3 port");
            System.exit(1);
        }
        try {
            // Create a new Server object
            s_portNumber = Integer.parseInt(args[0]);
            Socket m = new Socket(args[1], Integer.parseInt(args[2]));
            Socket m1 = new Socket(args[3], Integer.parseInt(args[4]));
            Socket m2 = new Socket(args[5], Integer.parseInt(args[6]));
            IResourceManager rm1 = (IResourceManager) m;
            IResourceManager rm2 = (IResourceManager) m1;
            IResourceManager rm3 = (IResourceManager) m2;

            Middleware server = new Middleware(s_serverName);
            // Dynamically generate the stub (client proxy)
            ServerSocket serverSocket = new ServerSocket(1099);

            System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_rmiPrefix + s_serverName + "'");
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
    }

    public TCPMiddleware(String name, IResourceManager rm1, IResourceManager rm2, IResourceManager rm3)
    {
        super(name);
    }
}
