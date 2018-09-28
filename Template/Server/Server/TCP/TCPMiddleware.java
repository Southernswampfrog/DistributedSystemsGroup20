package Server.TCP;

import Server.Common.Middleware;
import Server.Interface.IResourceManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;

public class TCPMiddleware extends Middleware {


    public static void main(String args[])
    {
        // Create the TCP server entry
      /*  if (args.length < 1) {
            System.out.println("Usage: java TCPMiddleware middleware server1, server 2, server3");
            System.exit(1);
        }*/
        try {
            Socket flights = new Socket(args[0], 6111);
            System.out.println("Connected to flights");
            Socket cars = new Socket(args[1], 6111);
            System.out.println("Connected to cars");
            Socket rooms = new Socket(args[2], 6111);
            System.out.println("Connected to rooms");
            ServerSocket server = new ServerSocket(6111);
            while (true) {
                try {
                    // Accept incoming connections.
                    Socket clientSocket = server.accept();
                    System.out.println("connected to " + clientSocket.getInetAddress());
                    BufferedReader bis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String js = bis.readLine();
                    OutputStream os;
                    JSONObject jsob = new JSONObject(js);
                    switch (jsob.getString("methodName")) {
                        case "AddFlight":
                            os = flights.getOutputStream();
                            PrintWriter pw = new PrintWriter(os);
                            pw.println(js);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(flights.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.write(js);
                            pw.flush();
                            break;
                        case "AddCars":
                            os = cars.getOutputStream();
                            os.write(js.getBytes());
                            os.flush();
                            os.close();
                        case "AddRooms":
                        case "AddCustomers":
                        case "AddCustomerID":
                        case "DeleteFlight":
                        case "DeleteCars":
                        case "DeleteRooms":
                        case "DeleteCustomer":
                        case "QueryFlight":
                        case "QueryCars":
                        case "QueryRooms":
                        case "QueryFlightPrice":
                        case "QueryCarsPrice":
                        case "QueryRoomsPrice":
                        case "ReserveFlight":
                        case "ReserveRoom":
                        case "ReserveCar":
                        case "bundle":
                    }
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

        // Create and install a security manager
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
    }

    public TCPMiddleware(String name, IResourceManager rm1, IResourceManager rm2, IResourceManager rm3)
    {
        super(name);
    }
}
