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
            // Accept incoming connections.
            while(true) {
                Socket clientSocket = server.accept();
                System.out.println("connected to " + clientSocket.getInetAddress());
                Thread t = new MiddlewareThread(clientSocket, rm);
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
                            rm.addFlight(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                            pw.println("Flight Added");
                            break;
                        case "AddCars":
                            rm.addCars(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numberOfCars"), jsob.getInt("price"));
                            pw.println("Car Added");
                            break;
                        case "AddRooms":
                            rm.addRooms(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numRooms"), jsob.getInt("price"));
                            pw.println("Room Added");
                            break;
                        case "AddCustomers":
                            rm.newCustomer(jsob.getInt("id"));
                            pw.println("Customer Added");
                            break;
                        case "AddCustomerID":
                            rm.newCustomer(jsob.getInt("id"),jsob.getInt("customerID"));
                            pw.println("Customer ID Added");
                            break;
                        case "DeleteFlight":
                            rm.deleteFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                            pw.println("Flight Deleted");
                            break;
                        case "DeleteCars":
                            rm.deleteCars(jsob.getInt("id"),jsob.getString("location"));
                            pw.println("Car Deleted");
                            break;
                        case "DeleteRooms":
                            rm.deleteRooms(jsob.getInt("id"),jsob.getString("location"));
                            pw.println("Room Deleted");
                            break;
                        case "DeleteCustomer":
                            rm.deleteCustomer(jsob.getInt("id"),jsob.getInt("customerID"));
                            pw.println("Customer Deleted");
                            break;
                        case "QueryFlight":
                            int availSeats = rm.queryFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                            s = "There are " + availSeats + "empty seats available on this Flight";
                            pw.println(s);
                            break;
                        case "QueryCars":
                            int availCars = rm.queryCars(jsob.getInt("id"),jsob.getString("location"));
                            s = "There are " + availCars + "empty seats available on this Flight";
                            pw.println(s);
                            break;
                        case "QueryRooms":
                            int availRooms = rm.queryRooms(jsob.getInt("id"),jsob.getString("location"));
                            s = "There are " + availRooms + "empty seats available on this Flight";
                            pw.println(s);
                            break;
                        case "QueryCustomer":
                            s = rm.queryCustomerInfo(jsob.getInt("id"),jsob.getInt("customerID"));
                            pw.println(s);
                            break;
                        case "QueryFlightPrice":
                            int flightPrice = rm.queryFlightPrice(jsob.getInt("id"),jsob.getInt("flightNumber"));
                            s = "Price of flight is" + flightPrice;
                            pw.println(s);
                            break;
                        case "QueryCarsPrice":
                            int carPrice = rm.queryCarsPrice(jsob.getInt("id"),jsob.getString("location"));
                            s = "Price of car is" + carPrice;
                            pw.println(s);
                            break;
                        case "QueryRoomsPrice":
                            int roomPrice = rm.queryRoomsPrice(jsob.getInt("id"),jsob.getString("location"));
                            s = "Price of room is" + roomPrice;
                            pw.println(s);
                            break;
                        case "ReserveFlight":
                            rm.reserveFlight(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getInt("flightNumber"));
                            pw.println("Flight Reserved");
                            break;
                        case "ReserveRoom":
                            rm.reserveRoom(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                            pw.println("Room Reserved");
                            break;
                        case "ReserveCar":
                            rm.reserveCar(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                            pw.println("Car Reservered");
                            break;
                        case "bundle":
                            rm.bundle(jsob.getInt("id"),jsob.getInt("customerID"),(Vector<String>)jsob.get("flightNumbers"),jsob.getString("location"),jsob.getBoolean("car"),jsob.getBoolean("room"));
                            pw.println("Bundle Reserved");
                            break;
                    }
                    pw.flush();


                } catch (Exception ioe) {
                    System.out.println("Exception encountered on accept. Ignoring. Stack Trace :");
                    ioe.printStackTrace();
                }
            }
        }
    }