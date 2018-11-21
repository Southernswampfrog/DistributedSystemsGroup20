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
                "Start",
                "AddFlight,1,1,10,10",
                "AddRooms,1,Montreal,15,15",
                "AddCustomerID,1,1",
                "ReserveFlight,1,1,1",
                "ReserveRoom,1,1,Montreal",
                "QueryCustomer,1,1",           // Returns flight-1, room-montreal
                "Commit,1",
                "Start",                        // [xid=2]
                "QueryCustomer,2,1",            // [flight-1, room-montreal]
                "Commit,2",
                "Start",                        // [xid=3]
                "AddCars,3,Monteal,20,20",
                "AddRooms,3,Montreal,10,10",
                "AddCustomerID,3,2",
                "ReserveFlight,3,1,1",
                "ReserveFlight,3,2,1",
                "QueryCustomer,3,1",            // [2xflight-1, room-montreal]
                "QueryCustomer,3,2",            // [flight-1]
                "Abort,3",
                "Start",                        // [xid=4]
                "QueryCustomer,4,1",            // [flight-1, room-montreal]
                "QueryCustomer,4,2",            // []
                "Commit,4",
                "Start",                        // [xid=5]
                "Start",                        // [xid=6]
                "ReserveFlight,5,1,1",
                "QueryCustomer,6,1",            // Blocked, timeout
                "Abort,5",
                "Start",                        // [xid=7]
                "QueryRooms,7,Montreal",
                "AddRooms,7,Montreal,5,5",      // Succeeds
                "Abort,7",
                "Start",        // [xid=8]
                "Start",        // [xid=9]
                "QueryFlight,8,1",
                "QueryRooms,9,Montreal",
                "AddRooms,8,Montreal,10,10",
                "AddFlight,9,1,10,10",
                "commit,9",
                "Start",                        // [xid=10]
                "AddFlight,10,1,10,10",
                "abort,10",
                "Start",                        // [xid=11]
                "QueryFlight,11,1",             // Quantity >= 1
                "QueryCars,11,Montreal",        // Quantity == 0
                "QueryRooms,11,Montreal",       // Quantity >= 1
                "QueryCustomer,11,1",           // [flight-1, room-montreal]
                "Bundle,11,1,1,Montreal,true,true",   // Fails, and does *not* reserve any flight or room
                "QueryCustomer,11,1",           // [flight-1, room-montreal]
                "Abort,11",
                "Start",                        // [xid=12]
                "AddFlight,12,10,10,10",
                "Commit,12",

                "Start",                        // [xid=13]
                "QueryFlight,13,10",            // Quantity == 10
                "DeleteFlight,13,10",
                "Abort,13",

                "Start",                        // [xid=14]
                "QueryFlight,14,10",            // Quantity == 10
                "DeleteFlight,14,10",
                "AddFlight,14,10,15,15",
                "Abort,14",

                "Start",                        // [xid=15]
                "QueryFlight,15,10"            // Quantity == 10
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
