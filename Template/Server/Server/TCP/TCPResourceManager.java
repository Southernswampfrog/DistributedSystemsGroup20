package Server.TCP;

import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;
import org.json.JSONObject;

import java.net.*;
import java.io.*;

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
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "AddRooms":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "AddCustomers":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "AddCustomerID":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "DeleteFlight":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "DeleteCars":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "DeleteRooms":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "DeleteCustomer":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "QueryFlight":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "QueryCars":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "QueryRooms":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "QueryFlightPrice":
                                rm.addCars(jsob.getInt("id"),jsob.getInt("flightNum"),jsob.getInt("flightSeats"), jsob.getInt("flightPrice"));
                                os.write("Car Added".getBytes());
                            case "QueryCarsPrice":
                            case "QueryRoomsPrice":
                            case "ReserveFlight":
                            case "ReserveRoom":
                            case "ReserveCar":
                            case "bundle":
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

