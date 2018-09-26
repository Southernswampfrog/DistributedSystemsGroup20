package Server.TCP;

import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;

import java.net.*;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;

public class TCPResourceManager extends ResourceManager implements Serializable{
    private static String s_serverName = "Server";
    private static int s_portNumber;

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            s_serverName = args[0];
            s_portNumber = Integer.parseInt(args[1]);
        }
        try {
            // Create a new Server object
            ServerSocket server = new ServerSocket(s_portNumber);
            System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_portNumber + "'");
            TCPResourceManager rm = new TCPResourceManager(s_serverName);
            IResourceManager resourceManager = (IResourceManager) UnicastRemoteObject.exportObject(rm, 0);
            while(true){
                    try {
                        // Accept incoming connections.
                        Socket clientSocket = server.accept();
                        OutputStream outToServer = clientSocket.getOutputStream();
                        new ObjectOutputStream(outToServer).writeObject(resourceManager);
                        System.out.println("sending interface");
                    } catch (Exception ioe) {
                        System.out.println("Exception encountered on accept. Ignoring. Stack Trace :");
                        ioe.printStackTrace();
                    }
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

