package Client;
import org.json.JSONArray;
import org.json.JSONML;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class TCPClient {
    public static String s_serverHost = "localhost";
    public static int s_serverPort;
    private static String s_serverName = "Server";
    private static String s_rmiPrefix = "group20";
    public OutputStream outputStream;
    public static Socket client;
    public PrintWriter pw;

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            s_serverHost = args[0];
        }
        if (args.length > 1)
        {
            s_serverPort = Integer.parseInt(args[1]);
        }
        if (args.length > 2)
        {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
            System.exit(1);
        }


        try {
            TCPClient tcpClient = new TCPClient();
            tcpClient.connectServer();
            tcpClient.start();
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TCPClient()
    {
        super();
    }

    public void connectServer()
    {
        connectServer(s_serverHost, s_serverPort);
    }

    public void connectServer(String server, int port)
    {
        try {
            boolean first = true;
            while (true) {
                try {
                    client = new Socket(s_serverHost, s_serverPort);
                    System.out.println("Connected to server [" + server + ":" + port + "/" + s_rmiPrefix + "]");
                    break;
                }
                catch (Exception e) {
                    if (first) {
                        System.out.println(e);
                        e.printStackTrace();
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start()
    {
        // Prepare for reading commands
        System.out.println();
        System.out.println("Location \"help\" for list of supported commands");

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        while (true)
        {
            // Read the next command
            String command = "";
            Vector<String> arguments = new Vector<String>();
            try {
                System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
                command = stdin.readLine().trim();
            }
            catch (IOException io) {
                System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
                io.printStackTrace();
                System.exit(1);
            }

            try {
                arguments = parse(command);
                Command cmd = Command.fromString((String)arguments.elementAt(0));
                try {
                    execute(cmd, arguments);
                }
                catch (Exception e) {
                    connectServer();
                    execute(cmd, arguments);
                }
            }
            catch (Exception e) {
                System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
                e.printStackTrace();
            }
        }
    }

    public void execute(Command cmd, Vector<String> arguments) throws NumberFormatException
    {
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            outputStream = new DataOutputStream(out);
            pw = new PrintWriter((outputStream));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        JSONObject method = new JSONObject();
        switch (cmd)
        {
            case Help:
            {
                if (arguments.size() == 1) {
                    System.out.println(Command.description());
                } else if (arguments.size() == 2) {
                    Command l_cmd = Command.fromString((String)arguments.elementAt(1));
                    System.out.println(l_cmd.toString());
                } else {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
                }
                break;
            }
            case AddFlight: {
                checkArgumentsCount(5, arguments.size());

                System.out.println("Adding a new flight [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));
                System.out.println("-Flight Seats: " + arguments.elementAt(3));
                System.out.println("-Flight Price: " + arguments.elementAt(4));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));
                int flightSeats = toInt(arguments.elementAt(3));
                int flightPrice = toInt(arguments.elementAt(4));

                try {
                    method.put("id", id);
                    method.put("flightNum", flightNum);
                    method.put("flightSeats", flightSeats);
                    method.put("flightPrice", flightPrice);
                    method.put("methodName", "AddFlight");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }
                break;
            }
            case AddCars: {
                checkArgumentsCount(5, arguments.size());

                System.out.println("Adding new cars [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));
                System.out.println("-Number of Cars: " + arguments.elementAt(3));
                System.out.println("-Car Price: " + arguments.elementAt(4));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                int numCars = toInt(arguments.elementAt(3));
                int price = toInt(arguments.elementAt(4));

                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("numCars", numCars);
                    method.put("price", price);
                    method.put("methodName", "AddCars");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case AddRooms: {
                checkArgumentsCount(5, arguments.size());

                System.out.println("Adding new rooms [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));
                System.out.println("-Number of Rooms: " + arguments.elementAt(3));
                System.out.println("-Room Price: " + arguments.elementAt(4));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                int numRooms = toInt(arguments.elementAt(3));
                int price = toInt(arguments.elementAt(4));

                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("numRooms", numRooms);
                    method.put("price", price);
                    method.put("methodName", "AddRooms");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }


            case AddCustomer: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Adding a new customer [id=" + arguments.elementAt(1) + "]");

                int id = toInt(arguments.elementAt(1));
                try {
                    method.put("id", id);
                    method.put("methodName", "AddCustomer");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }
                break;
            }


            case AddCustomerID: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Adding a new customer [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));

                try {
                    method.put("id", id);
                    method.put("customerID", customerID);

                    method.put("methodName", "AddCustomerID");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }


            case DeleteFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting a flight [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));

                try {
                    method.put("id", id);
                    method.put("flightNum", flightNum);
                    method.put("methodName", "DeleteFlight");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }

            case DeleteCars: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting all cars at a particular location [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("methodName", "DeleteCars");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }
                break;
            }

            case DeleteRooms: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting all rooms at a particular location [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("methodName", "DeleteRooms");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case DeleteCustomer: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting a customer from the database [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                try {
                    method.put("id", id);
                    method.put("customerID", customerID);
                    method.put("methodName", "DeleteCustomer");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case QueryFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying a flight [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));
                try {
                    method.put("id", id);
                    method.put("flightNum", flightNum);
                    method.put("methodName", "QueryFlight");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case QueryCars: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying cars location [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("methodName", "QueryCars");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case QueryRooms: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying rooms location [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("methodName", "QueryRooms");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

            //    System.out.println("Number of rooms at this location: " + numRoom);
                break;
            }
            case QueryCustomer: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying customer information [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                try {
                    method.put("id", id);
                    method.put("customerID", customerID);
                    method.put("methodName", "QueryCustomer");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case QueryFlightPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying a flight price [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));
                try {
                    method.put("id", id);
                    method.put("flightNum", flightNum);
                    method.put("methodName", "QueryFlightPrice");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case QueryCarsPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying cars price [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("methodName", "QueryCarsPrice");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case QueryRoomsPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying rooms price [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("methodName", "QueryRoomsPrice");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case ReserveFlight: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Reserving seat in a flight [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                System.out.println("-Flight Number: " + arguments.elementAt(3));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                int flightNum = toInt(arguments.elementAt(3));

                try {
                    method.put("id", id);
                    method.put("customerID", customerID);
                    method.put("flightNum", flightNum);
                    method.put("methodName", "ReserveFlight");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case ReserveCar: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Reserving a car at a location [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                System.out.println("-Car Location: " + arguments.elementAt(3));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                String location = arguments.elementAt(3);

                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("customerID", customerID);
                    method.put("methodName", "ReserveCar");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case ReserveRoom: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Reserving a room at a location [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                System.out.println("-Room Location: " + arguments.elementAt(3));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                String location = arguments.elementAt(3);

                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("customerID", customerID);
                    method.put("methodName", "ReserveRoom");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                break;
            }
            case Bundle: {
                if (arguments.size() < 7) {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mBundle command expects at least 7 arguments. Location \"help\" or \"help,<CommandName>\"");
                    break;
                }

                System.out.println("Reserving an bundle [id=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                for (int i = 0; i < arguments.size() - 6; ++i)
                {
                    System.out.println("-Flight Number: " + arguments.elementAt(3+i));
                }
                System.out.println("-Car Location: " + arguments.elementAt(arguments.size()-2));
                System.out.println("-Room Location: " + arguments.elementAt(arguments.size()-1));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                Vector<String> flightNumbers = new Vector<String>();
                for (int i = 0; i < arguments.size() - 6; ++i)
                {
                    flightNumbers.addElement(arguments.elementAt(3+i));
                }
                String location = arguments.elementAt(arguments.size()-3);
                boolean car = toBoolean(arguments.elementAt(arguments.size()-2));
                boolean room = toBoolean(arguments.elementAt(arguments.size()-1));
                try {
                    method.put("id", id);
                    method.put("location", location);
                    method.put("customerID", customerID);
                    method.put("car", car);
                    method.put("room", room);
                    method.put("flightNumbers", flightNumbers);
                    method.put("methodName", "bundle");
                    pw.println(method);
                    pw.flush();
                }
                catch (Exception e) {
                    System.out.println(e);
                }
                break;
            }
            case Quit:
                checkArgumentsCount(1, arguments.size());
                System.out.println("Quitting client");
                System.exit(0);
    }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            System.out.println(in.readLine());
        }
        catch (Exception e ){
            e.printStackTrace();
        }
    }



    public static Vector<String> parse(String command)
    {
        Vector<String> arguments = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(command,",");
        String argument = "";
        while (tokenizer.hasMoreTokens())
        {
            argument = tokenizer.nextToken();
            argument = argument.trim();
            arguments.add(argument);
        }
        return arguments;
    }
    public static void checkArgumentsCount(Integer expected, Integer actual) throws IllegalArgumentException
    {
        if (expected != actual)
        {
            throw new IllegalArgumentException("Invalid number of arguments. Expected " + (expected - 1) + ", received " + (actual - 1) + ". Location \"help,<CommandName>\" to check usage of this command");
        }
    }

    public static int toInt(String string) throws NumberFormatException
    {
        return (new Integer(string)).intValue();
    }

    public static boolean toBoolean(String string)// throws Exception
    {
        return (new Boolean(string)).booleanValue();
    }
}
