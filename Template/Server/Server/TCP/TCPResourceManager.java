package Server.TCP;

import Server.Common.ResourceManager;
import java.rmi.registry.Registry;
import java.net.*;

public class TCPResourceManager extends ResourceManager {
    private static String s_serverName = "Server";
    private static int s_portNumber;

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            s_serverName = args[0];
            s_portNumber = Integer.parseInt(args[2]);
        }

        // Create the RMI server entry
        try {
            // Create a new Server object
            ServerSocket server = new ServerSocket(s_portNumber);
            System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_portNumber + "'");
            while(true){

            }
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TCPResourceManager(String name)
    {
        super(name);
    }
}

