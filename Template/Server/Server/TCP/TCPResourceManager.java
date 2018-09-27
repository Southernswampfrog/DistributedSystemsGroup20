package Server.TCP;

import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;
import org.json.JSONObject;

import java.net.*;
import java.io.*;

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
            IResourceManager irm = rm;
            while(true){
                    try {
                        // Accept incoming connections.
                        Socket clientSocket = server.accept();
                        System.out.println("connected to " + clientSocket.getInetAddress());
                        BufferedReader bis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String jsob = bis.readLine();
                        System.out.println(jsob);
                        JSONObject jsrob = new JSONObject(jsob);
                        System.out.println(jsrob.get("methodParams"));
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

