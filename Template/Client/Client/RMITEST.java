package Client;

import Server.Interface.IResourceManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class RMITEST extends Client {

    private static String s_serverHost = "localhost";
    private static int s_serverPort = 1099;
    private static String s_serverName = "Server";


    private static String s_rmiPrefix = "group20";

    public static void main(String args[]) {
        if (args.length > 0) {
            s_serverHost = args[0];
        }
        if (args.length > 1) {
            s_serverName = args[1];
        }

        // Set the security policy
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // Get a reference to the RMIRegister
        try {
            RMITEST client = new RMITEST();
            client.connectServer();
            client.start();
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mClient exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public RMITEST() {
        super();
    }

    public void connectServer() {
        connectServer(s_serverHost, s_serverPort, s_serverName);
    }

    public void connectServer(String server, int port, String name) {
        try {
            boolean first = true;
            while (true) {
                try {
                    Registry registry = LocateRegistry.getRegistry(server, port);
                    System.out.println(registry);
                    m_resourceManager = (IResourceManager) registry.lookup(s_rmiPrefix + name);
                    System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
                    break;
                } catch (NotBoundException | RemoteException e) {
                    if (first) {
                        System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        String[] testCases = {
                "CrashResourceManager,2,Flights",
                "CrashMiddleware,8",
                "start",
                "AddFlight,1,1,1,1",
                "AddFlight,1,2,2,2",
                "AddCars,1,1,1,1",
                "AddRooms,1,1,1,1",
                "AddCustomerID,1,1",
                "AddCustomer,1",
                "bundle,1,1,1,2,1,false,true",
                "commit,1"
        };
        for (int i = 0; i < testCases.length; i++) {
            // Read the next command
            String command = "";
            Vector<String> arguments = new Vector<String>();
            System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
            command = testCases[i];
            try {
                arguments = parse(command);
                Command cmd = Command.fromString((String) arguments.elementAt(0));
                try {
                    execute(cmd, arguments);
                } catch (Exception e) {
                    connectServer();
                    execute(cmd, arguments);
                }
            } catch (Exception e) {
                System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0mUncaught exception");
                e.printStackTrace();
            }
        }
    }
}
