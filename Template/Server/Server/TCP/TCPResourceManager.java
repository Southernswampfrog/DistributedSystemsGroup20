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
                        switch (jsob.getString("methodName")) {
                            case "AddFlight":
                                rm.addFlight(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Flight Added".getBytes());
                            case "AddCars":
                                rm.addCars(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numberOfCars"), jsob.getInt("price"));
                                os.write("Car Added".getBytes());
                            case "AddRooms":
                                rm.addRooms(jsob.getInt("id"),jsob.getString("location"),jsob.getInt("numRooms"), jsob.getInt("price"));
                                os.write("Room Added".getBytes());
                            case "AddCustomers":
                                rm.newCustomer(jsob.getInt("id"));
                                os.write("Customer Added".getBytes());
                            case "AddCustomerID":
                                rm.newCustomer(jsob.getInt("id"),jsob.getInt("cid"));
                                os.write("Customer ID Added".getBytes());
                            case "DeleteFlight":
                                rm.deleteFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                                os.write("Flight Deleted".getBytes());
                            case "DeleteCars":
                                rm.deleteCars(jsob.getInt("id"),jsob.getString("location"));
                                os.write("Car Deleted".getBytes());
                            case "DeleteRooms":
                                rm.deleteRooms(jsob.getInt("id"),jsob.getString("location"));
                                os.write("Room Deleted".getBytes());
                            case "DeleteCustomer":
                                rm.deleteCustomer(jsob.getInt("id"),jsob.getInt("customerID"));
                                os.write("Customer Deleted".getBytes());
                            case "QueryFlight":
                                int availSeats = rm.queryFlight(jsob.getInt("id"),jsob.getInt("flightNum"));
                                String s = "There are " + availSeats + "empty seats available on this Flight";
                                os.write(s.getBytes());
                            case "QueryCars":
                                int availCars = rm.queryCars(jsob.getInt("id"),jsob.getString("location"));
                                os.write("".getBytes());
                            case "QueryRooms":
                                rm.queryRooms(jsob.getInt("id"),jsob.getString("location"));
                                os.write("".getBytes());
                            case "QueryCustomer":
                                rm.queryCustomerInfo(jsob.getInt("id"),jsob.getInt("customerID"));
                                os.write("".getBytes());
                            case "QueryFlightPrice":
                                rm.queryFlightPrice(jsob.getInt("id"),jsob.getInt("flightNumber"));
                                os.write("".getBytes());
                            case "QueryCarsPrice":
                                rm.queryCarsPrice(jsob.getInt("id"),jsob.getString("location"));
                            case "QueryRoomsPrice":
                                rm.queryRoomsPrice(jsob.getInt("id"),jsob.getString("location"));
                            case "ReserveFlight":
                                rm.reserveFlight(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getInt("flightNumber"));
                            case "ReserveRoom":
                                rm.reserveRoom(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                            case "ReserveCar":
                                rm.reserveCar(jsob.getInt("id"),jsob.getInt("customerID"),jsob.getString("location"));
                            case "bundle":
                                rm.bundle(jsob.getInt("id"),jsob.getInt("customerID"),(Vector<String>)jsob.get("flightNumbers"),jsob.getString("location"),jsob.getBoolean("car"),jsob.getBoolean("room"));
                        }
                        os.flush();
                        os.close();

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

