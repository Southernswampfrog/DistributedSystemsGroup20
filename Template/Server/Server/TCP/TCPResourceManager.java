package Server.TCP;

import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.util.Vector;

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
            while(true){
                    try {
                        // Accept incoming connections.
                        Socket clientSocket = server.accept();
                        System.out.println("connected to " + clientSocket.getInetAddress());
                        BufferedReader bis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String js = bis.readLine();
                        JSONObject jsob = new JSONObject(js);
                        OutputStream os  = clientSocket.getOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        String s, switchCase = jsob.getString("methodName");
                        System.out.println(switchCase);
                        switch (switchCase) {
                            case "AddFlight":
                                rm.addFlight(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                pw.println("Flight Added");
                                break;
                            case "AddCars":
                                rm.addCars(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numberOfCars"), jsob.getInt("price"));
                                pw.println("Car Added");
                            case "AddRooms":
                                rm.addRooms(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numRooms"), jsob.getInt("price"));
                                pw.println("Room Added");
                            case "AddCustomers":
                                rm.newCustomer(jsob.getInt("id"));
                                pw.println("Customer Added");
                            case "AddCustomerID":
                                rm.newCustomer(jsob.getInt("id"),jsob.getInt("customerID"));
                                pw.println("Customer ID Added");
                            case "DeleteFlight":
                                rm.deleteFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                                pw.println("Flight Deleted");
                            case "DeleteCars":
                                rm.deleteCars(jsob.getInt("id"),jsob.getString("location"));
                                pw.println("Car Deleted");
                            case "DeleteRooms":
                                rm.deleteRooms(jsob.getInt("id"),jsob.getString("location"));
                                pw.println("Room Deleted");
                            case "DeleteCustomer":
                                rm.deleteCustomer(jsob.getInt("id"),jsob.getInt("customerID"));
                                pw.println("Customer Deleted");
                            case "QueryFlight":
                                int availSeats = rm.queryFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                                s = "There are " + availSeats + "empty seats available on this Flight";
                                pw.println(s);
                            case "QueryCars":
                                int availCars = rm.queryCars(jsob.getInt("id"),jsob.getString("location"));
                                s = "There are " + availCars + "empty seats available on this Flight";
                                pw.println(s);
                            case "QueryRooms":
                                int availRooms = rm.queryRooms(jsob.getInt("id"),jsob.getString("location"));
                                s = "There are " + availRooms + "empty seats available on this Flight";
                                pw.println(s);
                            case "QueryCustomer":
                                s = rm.queryCustomerInfo(jsob.getInt("id"),jsob.getInt("customerID"));
                                pw.println(s);
                            case "QueryFlightPrice":
                                int flightPrice = rm.queryFlightPrice(jsob.getInt("id"),jsob.getInt("flightNumber"));
                                s = "Price of flight is" + flightPrice;
                                pw.println(s);
                            case "QueryCarsPrice":
                                int carPrice = rm.queryCarsPrice(jsob.getInt("id"),jsob.getString("location"));
                                s = "Price of car is" + carPrice;
                                pw.println(s);
                            case "QueryRoomsPrice":
                                int roomPrice = rm.queryRoomsPrice(jsob.getInt("id"),jsob.getString("location"));
                                s = "Price of room is" + roomPrice;
                                pw.println(s);
                            case "ReserveFlight":
                                rm.reserveFlight(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getInt("flightNumber"));
                                pw.println("Flight Reserved");
                            case "ReserveRoom":
                                rm.reserveRoom(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                                pw.println("Room Reserved");
                            case "ReserveCar":
                                rm.reserveCar(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                                pw.println("Car Reservered");
                            case "bundle":
                                rm.bundle(jsob.getInt("id"),jsob.getInt("customerID"),(Vector<String>)jsob.get("flightNumbers"),jsob.getString("location"),jsob.getBoolean("car"),jsob.getBoolean("room"));
                                pw.println("Bundle Reserved");
                        }
                        pw.flush();


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

