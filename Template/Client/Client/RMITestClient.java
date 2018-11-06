package Client;

import Server.Interface.IResourceManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class RMITestClient extends Client {

    private static String s_serverHost = "localhost";
    private static int s_serverPort = 1099;
    private static String s_serverName = "Server";
    private static boolean multiClient = false;
    private static int numberOfClients = 0;
    private static int numberOfTransPerS = 0;
    private int numberOfRuns = 1;


    private static String s_rmiPrefix = "group20";

    public static void main(String args[]) {
        if (args.length > 0) {
            s_serverHost = args[0];
        }
        if (args.length > 1) {
            s_serverName = args[1];
        }
        if (args.length > 2) {
            numberOfClients = Integer.parseInt(args[2]);
            multiClient = numberOfClients > 1;
        }
        if (args.length > 3) {
            numberOfTransPerS = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
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
            for (int i = 0; i < numberOfClients; i++) {
                new Thread(new RMITestClientThread(numberOfClients, numberOfTransPerS, numberOfRuns, i)).start();
            }
        } else {
            System.out.println();

            String[] testScheduleSteps = {"Add", "Add", "Reserve", "Query", "Delete"};
            long[] averageRespTimesSingleRM = new long[testScheduleSteps.length];
            ArrayList<Long>[] respTimesSingleRM = new ArrayList[testScheduleSteps.length];
            long[] averageRespTimesAllRM = new long[testScheduleSteps.length];
            ArrayList<Long>[] respTimesAllRM = new ArrayList[testScheduleSteps.length];
            for (int i = 0; i < respTimesSingleRM.length; i++) {
                respTimesSingleRM[i] = new ArrayList<Long>();
            }
            for (int i = 0; i < respTimesAllRM.length; i++) {
                respTimesAllRM[i] = new ArrayList<Long>();
            }
            for (int j = 0; j < numberOfRuns; j++) {
                ArrayList<String> testCasesSingleRM = new ArrayList<String>();
                for (String testScheduleStep : testScheduleSteps) {
                    testCasesSingleRM.addAll(parameterizedTransactions(j, false, testScheduleStep));
                }
                ArrayList<String> testCasesAllRM = new ArrayList<String>();
                for (String testScheduleStep : testScheduleSteps) {
                    testCasesAllRM.addAll(parameterizedTransactions(j, true, testScheduleStep));
                }
                for (int i = 0; i < testCasesSingleRM.size(); i++) {
                    // Read the next command
                    String command = "";
                    Vector<String> arguments = new Vector<String>();
                    System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
                    command = testCasesSingleRM.get(i);
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
                    respTimesSingleRM[i % 5].add(endTime - startTime);
                    try {
                        //TimeUnit.SECONDS.sleep(3);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                for (int i = 0; i < testCasesAllRM.size(); i++) {
                    // Read the next command
                    String command = "";
                    Vector<String> arguments = new Vector<String>();
                    System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
                    command = testCasesAllRM.get(i);
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
                    respTimesAllRM[i % 5].add(endTime - startTime);
                    try {
                        //TimeUnit.SECONDS.sleep(3);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

            }
            for (int i = 0; i < respTimesSingleRM.length; i++) {
                long averageSingleRM = 0;
                for (long time : respTimesSingleRM[i]) {
                    averageSingleRM += time;
                }
                averageRespTimesSingleRM[i] = averageSingleRM / numberOfRuns;
                //System.out.println(respTimesSingleRM[i]);
            }
            for (int k = 0; k < averageRespTimesSingleRM.length; k++) {
                System.out.println(averageRespTimesSingleRM[k] + "---" + testScheduleSteps[k]);
            }
            for (int i = 0; i < respTimesAllRM.length; i++) {
                long averageAllRM = 0;
                for (long time : respTimesAllRM[i]) {
                    averageAllRM += time;
                }
                averageRespTimesAllRM[i] = averageAllRM / numberOfRuns;
                //System.out.println(respTimesAllRM[i]);
            }
            for (int k = 0; k < averageRespTimesAllRM.length; k++) {
                System.out.println(averageRespTimesAllRM[k] + "---" + testScheduleSteps[k]);
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
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Cars," + paramNumber + ",location" + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Rooms," + paramNumber + ",location" + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
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
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
                    transactions.add(type + "Flight," + paramNumber + "," + paramNumber + "," + paramNumber + "," + paramNumber + "\r");
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

class RMITestClientThread extends RMITestClient implements Runnable {
    private int numberOfClients;
    private int numberOfTransPerS;
    private int numberOfRuns;
    private int threadnumber;

    public RMITestClientThread(int numberOfClients, int numberOfTransPerS, int numberOfRuns, int threadnumber) {
        this.numberOfClients = numberOfClients;
        this.numberOfTransPerS = numberOfTransPerS;
        this.numberOfRuns = numberOfRuns;
        this.threadnumber = threadnumber;
    }

    public void run() {
        System.out.println("IN MULTI CLIENT");
        long interval = 1000000000 * numberOfClients / numberOfTransPerS;
        System.out.println(interval);
        // Prepare for reading commands
        System.out.println();


        long[] averageRespTimesSingleRM = new long[26];
        ArrayList<Long>[] respTimesSingleRM = new ArrayList[26];
        for (int i = 0; i < respTimesSingleRM.length; i++) {
            respTimesSingleRM[i] = new ArrayList<Long>();
        }
        for (int j = 0; j < numberOfRuns; j++) {
            String[] testCases = {
                    "start\r",
                    "AddCustomerID," + (j + 1) + "," + (100+threadnumber) +"\r",
                    "AddCustomer," + (j + 1) + "\r",
               /*     "AddFlight," + (j + 1) + ",2,2," + (2+threadnumber) +"\r",
                    "AddRooms," + (j + 1) + ",2,2," + (2+threadnumber) +"\r",
                    "AddCars," + (j + 1) + ",2,2," + (2+threadnumber) +"\r",
                    "QueryFlight," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryCars," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryRooms," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryCustomer," + (j + 1) + "," + (100+threadnumber) +"\r",
                    "QueryFlightPrice," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryRoomsPrice," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryCarsPrice," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "ReserveFlight," + (j + 1) + ",100," + (2+threadnumber) +"\r",
                    "ReserveCar," + (j + 1) + ",100," + (2+threadnumber) +"\r",
                    "ReserveRoom," + (j + 1) + ",100," + (2+threadnumber) +"\r",
                    "QueryFlight," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryCars," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryRooms," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "Bundle," + (j + 1) + ",100,2," + (2+threadnumber) +",true,true\r",
                    "Bundle," + (j + 1) + ",100,2," + (2+threadnumber) +",true,true\r",
                    "QueryFlight," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryCars," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryRooms," + (j + 1) + "," + (2+threadnumber) +"\r",
                    "QueryCustomer," + (j + 1) + "," + (1000+threadnumber) +"\r",*/
                    "commit," + (j + 1) + "\r",
            };
            for (int i = 0; i < testCases.length; i++) {
                // Read the next command
                String command = "";
                Vector<String> arguments = new Vector<String>();
                System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
                command = testCases[i];
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
                respTimesSingleRM[i].add(endTime - startTime);
                System.out.println(interval - (endTime - startTime));
                try {
                    TimeUnit.NANOSECONDS.sleep(interval - (endTime - startTime));
                    //TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        for (int i = 0; i < respTimesSingleRM.length; i++) {
            long averageSingleRM = 0;
            for (long time : respTimesSingleRM[i]) {
                averageSingleRM += time;
            }
            averageRespTimesSingleRM[i] = averageSingleRM / numberOfRuns;
            //System.out.println(respTimesSingleRM[i]);
        }
        for (int k = 0; k < averageRespTimesSingleRM.length; k++) {
            System.out.println(averageRespTimesSingleRM[k] + "---" + k);
        }
    }
}
