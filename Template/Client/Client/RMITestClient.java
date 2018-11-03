package Client;

import Server.Interface.IResourceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class RMITestClient extends Client {

    private static String s_serverHost = "localhost";
    private static int s_serverPort = 1099;
    private static String s_serverName = "Server";

    //TODO: REPLACE 'ALEX' WITH YOUR GROUP NUMBER TO COMPILE
    private static String s_rmiPrefix = "group20";

    public static void main(String args[]) {
        if (args.length > 0) {
            s_serverHost = args[0];
        }
        if (args.length > 1) {
            s_serverName = args[1];
        }
        if (args.length > 2) {
            System.err.println((char) 27 + "[31;1mClient exception: " + (char) 27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
            System.exit(1);
        }

        // Set the security policy
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // Get a reference to the RMIRegister
        try {
            RMITestClient client = new RMITestClient();
            client.connectServer();
            client.start();
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mClient exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public RMITestClient() {
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
        // Prepare for reading commands
        System.out.println();
        System.out.println("Location \"help\" for list of supported commands");

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String[] testCases = {
                //   "AddCustomerID,2,2\r",
                "start\r",
                "QueryFlight,1,2\r",
                "start\r",
                //"QueryCars,0,Italy\r",
                "AddFlight,1,2,2,2\r",
                "QueryFlight,2,2\r",
                "commit,1\r",
                "commit,2\r",
                //"ReserveFlight,2,2,2\r",
                //"AddRooms,2,Italy,2,2\r",
                //"ReserveRoom,2,2,\"Italy\"\r",
                //"AddCars,2,Italy,2,2\r",
                //"ReserveCar,2,2,Italy\r",
                //"QueryCustomer,2,2",
                //"QueryCars,2,Italy\r",
                //"Bundle,2,2,2,3,Italy,true,true\r",
                "quit\r"};
        for (String i : testCases) {
            // Read the next command
            String command = "";
            Vector<String> arguments = new Vector<String>();
            System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
            command = i;
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

