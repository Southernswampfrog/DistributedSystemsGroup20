package Server.TCP;

import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.Vector;

public class TCPResourceManager extends ResourceManager{
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
            // Accept incoming connections.
            while(true) {
                Socket clientSocket = null;
                try {
                    clientSocket = server.accept();
                    System.out.println("connected to " + clientSocket.getInetAddress());
                    Thread t = new MiddlewareThread(clientSocket, rm);
                    t.start();
                }
                catch (Exception e) {
                    clientSocket.close();
                    System.out.println("Connection Error");
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
class MiddlewareThread extends Thread{

        final Socket clientSocket;
        final ResourceManager rm;
        // Constructor
        public MiddlewareThread(Socket s, ResourceManager rm) {
            this.clientSocket = s;
            this.rm = rm;
        }

        public void run(){
            while(true){
                try {
                    BufferedReader bis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String js = bis.readLine();
                    JSONObject jsob = new JSONObject(js);
                    OutputStream os  = clientSocket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);
                    String s, switchCase = jsob.getString("methodName");
                    switch (switchCase) {
                        case "AddFlight":
                            boolean success = rm.addFlight(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                            if (success) {
                                pw.println("Flight Added");
                            }
                            else {
                                pw.println("Flight already exists");
                            }
                            pw.flush();

                            break;
                        case "AddCars":
                            success = rm.addCars(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numCars"), jsob.getInt("price"));
                            if (success) {
                                pw.println("Car Added");
                            }
                            else {
                                pw.println("Car already exists");
                            }
                            pw.flush();
                            break;
                        case "AddRooms":
                            success = rm.addRooms(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numRooms"), jsob.getInt("price"));
                            pw.println("Room Added");
                            if (success) {
                                pw.println("Room Added");
                            }
                            else {
                                pw.println("Room already exists");
                            }
                            pw.flush();

                            break;
                        case "AddCustomer":
                            int custID = rm.newCustomer(jsob.getInt("id"));
                            pw.println("Customer Added with id " + custID);
                            pw.flush();

                            break;
                        case "AddCustomerID":
                            success = rm.newCustomer(jsob.getInt("id"),jsob.getInt("customerID"));
                            if (success) {
                                pw.println("Customer Added");
                            }
                            else {
                                pw.println("Customer already exists");
                            }
                            pw.flush();

                            break;
                        case "DeleteFlight":
                            success = rm.deleteFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                            if (success) {
                                pw.println("Flight Deleted");
                            }
                            else {
                                pw.println("No Flight exists");
                            }
                            pw.flush();

                            break;
                        case "DeleteCars":
                            success = rm.deleteCars(jsob.getInt("id"),jsob.getString("location"));
                            if (success) {
                                pw.println("Car Deleted");
                            }
                            else {
                                pw.println("Car does not exist");
                            }
                            pw.flush();

                            break;
                        case "DeleteRooms":
                            success = rm.deleteRooms(jsob.getInt("id"),jsob.getString("location"));
                            if (success) {
                                pw.println("Room Deleted");
                            }
                            else {
                                pw.println("Room does not exist");
                            }
                            pw.flush();

                            break;
                        case "DeleteCustomer":
                            success = rm.deleteCustomer(jsob.getInt("id"),jsob.getInt("customerID"));

                            if (success) {
                                pw.println("Customer Deleted");
                            }
                            else {
                                pw.println("Customer does not exist");
                            }
                            pw.flush();
                            break;
                        case "QueryFlight":
                            int availSeats = rm.queryFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                            s = "There are " + availSeats + "empty seats available on this flight";
                            pw.println(s);
                            pw.flush();
                            break;
                        case "QueryCars":
                            int availCars = rm.queryCars(jsob.getInt("id"),jsob.getString("location"));
                            s = "There are " + availCars + "available cars.";
                            pw.println(s);
                            pw.flush();
                            break;
                        case "QueryRooms":
                            int availRooms = rm.queryRooms(jsob.getInt("id"),jsob.getString("location"));
                            s = "There are " + availRooms + "available rooms.";
                            pw.println(s);
                            pw.flush();
                            break;
                        case "QueryCustomer":
                            s = rm.queryCustomerInfo(jsob.getInt("id"),jsob.getInt("customerID"));
                            pw.println(s);
                            pw.flush();
                            break;
                        case "QueryFlightPrice":
                            int flightPrice = rm.queryFlightPrice(jsob.getInt("id"),jsob.getInt("flightNum"));
                            s = "Price of flight is" + flightPrice;
                            pw.println(s);
                            pw.flush();
                            break;
                        case "QueryCarsPrice":
                            int carPrice = rm.queryCarsPrice(jsob.getInt("id"),jsob.getString("location"));
                            s = "Price of car is" + carPrice;
                            pw.println(s);
                            pw.flush();

                            break;
                        case "QueryRoomsPrice":
                            int roomPrice = rm.queryRoomsPrice(jsob.getInt("id"),jsob.getString("location"));
                            s = "Price of room is" + roomPrice;
                            pw.println(s);
                            pw.flush();

                            break;
                        case "ReserveFlight":
                            success = rm.reserveFlight(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getInt("flightNum"));
                            if (success) {
                                pw.println("Flight Reserved");
                            }
                            else {
                                pw.println("Flight cannot be reserved");
                            }
                            pw.flush();

                            break;
                        case "ReserveRoom":
                            success = rm.reserveRoom(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                            if (success) {
                                pw.println("Room Reserved");
                            }
                            else {
                                pw.println("Room cannot be reserved");
                            }
                            pw.flush();

                            break;
                        case "ReserveCar":
                            success = rm.reserveCar(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                            if (success) {
                                pw.println("Car Reserved");
                            }
                            else {
                                pw.println("Car cannot be reserved");
                            }
                            pw.flush();

                            break;
                        case "AnalyticsFlight":
                            break;
                        case "AnalyticsCar":
                            break;
                        case "AnalyticsRoom":
                            break;
                        case "AnalyticsCustomer":
                            break;
                        case "bundle":
                            rm.bundle(jsob.getInt("id"),jsob.getInt("customerID"),(Vector<String>)jsob.get("flightNumbers"),jsob.getString("location"),jsob.getBoolean("car"),jsob.getBoolean("room"));
                            pw.println("Bundle Reserved");
                            pw.flush();

                            break;
                    }

                } catch (Exception ioe) {
                    System.out.println("Exception encountered on accept. Ignoring. Stack Trace :");
                    ioe.printStackTrace();
                }
            }
        }
    }