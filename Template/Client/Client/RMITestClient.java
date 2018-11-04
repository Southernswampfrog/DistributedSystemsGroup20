package Client;

import Server.Interface.IResourceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class RMITestClient extends Client {

    private static String s_serverHost = "localhost";
    private static int s_serverPort = 1099;
    private static String s_serverName = "Server";
    private static boolean multiClient = false;
    private int numberOfRuns = 50000;


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
            multiClient = args[2].equals("true");
        }
        if (args.length > 3) {
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
        if (multiClient) {
            // Prepare for reading commands
            System.out.println();
            System.out.println("Location \"help\" for list of supported commands");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String[] testCases = {
                    "start\r",
                    "AddCustomerID,1,100\r",
                    "AddCustomer,1\r",
                    "AddFlight,1,2,2,2\r",
                    "AddRooms,1,2,2,2\r",
                    "AddCars,1,2,2,2\r",
                    "QueryFlight,1,2\r",
                    "QueryCars,1,2\r",
                    "QueryRooms,1,2\r",
                    "QueryCustomer,1,100\r",
                    "QueryFlightPrice,1,2\r",
                    "QueryRoomsPrice,1,2\r",
                    "QueryCarsPrice,1,2\r",
                    "ReserveFlight,1,100,2\r",
                    "ReserveCar,1,100,2\r",
                    "ReserveRoom,1,100,2\r",
                    "QueryFlight,1,2\r",
                    "QueryCars,1,2\r",
                    "QueryRooms,1,2\r",
                    "Bundle,1,100,2,2,true,true\r",
                    "Bundle,1,100,2,2,true,true\r",
                    "QueryFlight,1,2\r",
                    "QueryCars,1,2\r",
                    "QueryRooms,1,2\r",
                    "QueryCustomer,1,100\r",
                    "commit,1\r",
                    "quit\r"};
            long[] averageRespTimes = new long[testCases.length];
            ArrayList<Long>[] respTimes = new ArrayList[testCases.length];
            for (int i = 0; i < respTimes.length; i++) {
                respTimes[i] = new ArrayList<Long>();
            }
            for (int i = 0; i < testCases.length; i++) {
                // Read the next command
                String command = "";
                Vector<String> arguments = new Vector<String>();
                System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
                command = testCases[i];
                int numberOfRuns = 1;
                for (int j = 0; j < numberOfRuns; j++) {
                    long startTime = System.nanoTime();
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
                    long endTime = System.nanoTime();
                    respTimes[i].add(endTime - startTime);
                }
                long average = 0;
                for (long time : respTimes[i]) {
                    average += time;
                }
                averageRespTimes[i] = average / numberOfRuns;
                System.out.println(respTimes[i]);
                for (int k = 0; k < averageRespTimes.length; k++) {
                    System.out.println(averageRespTimes[k] + "---" + testCases[k]);
                }

                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } else {
            System.out.println();

            String[] testScheduleSteps = {"Add", "Add", "Query", "Reserve", "Delete"};
            long[] averageRespTimes = new long[testScheduleSteps.length];
            ArrayList<Long>[] respTimes = new ArrayList[testScheduleSteps.length];
            for (int i = 0; i < respTimes.length; i++) {
                respTimes[i] = new ArrayList<Long>();
            }
            for (int j = 0; j < numberOfRuns; j++) {
                ArrayList<String> testCases = new ArrayList<String>();
                for (String testScheduleStep : testScheduleSteps) {
                    testCases.addAll(parameterizedTransactions(j, false, testScheduleStep));
                }
                for (int i = 0; i < testCases.size(); i++) {
                    // Read the next command
                    String command = "";
                    Vector<String> arguments = new Vector<String>();
                    System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
                    command = testCases.get(i);
                    long startTime = System.nanoTime();
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
                    long endTime = System.nanoTime();
                    respTimes[i].add(endTime - startTime);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
            for (int i = 0; i < respTimes.length; i++) {
                long average = 0;
                for (long time : respTimes[i]) {
                    average += time;
                }
            averageRespTimes[i] = average / numberOfRuns;
            System.out.println(respTimes[i]);
            }
            for (int k = 0; k < averageRespTimes.length; k++) {
                System.out.println(averageRespTimes[k] + "---" + testScheduleSteps[k]);
            }
        }
    }

    public static ArrayList<String> parameterizedTransactions(int paramNumber, boolean allRms, String type) {
        ArrayList<String> transactions = new ArrayList<String>();
        if (allRms) {
            switch (type) {
                case "Query":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Cars," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Rooms," + paramNumber + "," + paramNumber + "\r");
                    break;
                case "Add":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Cars," + paramNumber + ",location" + paramNumber + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Rooms," + paramNumber + ",location" + paramNumber + paramNumber + "," + paramNumber + "\r");
                    break;
                case "Delete":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Cars," + paramNumber + ",location" + paramNumber + "\r");
                    transactions.add(type + "Rooms," + paramNumber + ",location" + paramNumber + "\r");
                    break;
                case "Reserve":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Cars," + paramNumber + "," + paramNumber + ",location" + paramNumber + "\r");
                    transactions.add(type + "Rooms," + paramNumber + "," + paramNumber + ",location" + paramNumber + "\r");
                    break;
            }
        } else {
            switch (type) {
                case "Query":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    break;
                case "Add":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + paramNumber + "," + paramNumber + "\r");
                    break;
                case "Delete":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "\r");
                    break;
                case "Reserve":
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    break;
            }
        }
        return transactions;
    }
}
