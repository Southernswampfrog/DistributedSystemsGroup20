package Server.TCP;

import Server.Common.Middleware;
import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.*;
import java.net.*;

public class TCPMiddleware extends Middleware {


    public static void main(String args[]) {

        Middleware middleware = new Middleware("middleware");
        try {
            Socket flights = new Socket(args[0], 6111);
            System.out.println("Connected to flights");
            Socket cars = new Socket(args[1], 6111);
            System.out.println("Connected to cars");
            Socket rooms = new Socket(args[2], 6111);
            System.out.println("Connected to rooms");
            ServerSocket server = new ServerSocket(6111);


            while (true) {
                Socket clientSocket = null;
                try {
                    // Accept incoming connections.
                    clientSocket = server.accept();
                    System.out.println("connected to " + clientSocket.getInetAddress());
                    Thread t = new ClientThread(clientSocket, flights, cars, rooms);
                    t.start();
                } catch (Exception e) {
                    clientSocket.close();
                    System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    public TCPMiddleware(String name, IResourceManager rm1, IResourceManager rm2, IResourceManager rm3)
        {
            super(name);
        }
    }

    class ClientThread extends Thread {


        final Socket clientSocket;
        final Socket flights;
        final Socket cars;
        final Socket rooms;
        final Middleware middleware;

        // Constructor
        public ClientThread(Socket s, Socket flights, Socket cars, Socket rooms) {
            this.clientSocket = s;
            this.flights = flights;
            this.cars = cars;
            this.rooms = rooms;
            this.middleware = new Middleware("tcp_middleware");
        }

        public void run() {
            while(true) {
                try {
                    BufferedReader bis = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String js = bis.readLine();
                    System.out.println(js);
                    if (js == null) {
                        clientSocket.close();
                        break;
                    }
                    OutputStream os;
                    JSONObject jsob = new JSONObject(js);
                    String method = (jsob.getString("methodName"));
                    if (method.contains("Flight")) {
                        os = flights.getOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                        bis = new BufferedReader(new InputStreamReader(flights.getInputStream()));
                        js = bis.readLine();
                        os = clientSocket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                    } else if (method.contains("Car")) {
                        os = cars.getOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                        bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                        js = bis.readLine();
                        os = clientSocket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                    } else if (method.contains("Room")) {
                        os = rooms.getOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                        bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                        js = bis.readLine();
                        os = clientSocket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                    } else if (method.equals("bundle")) {
                        JSONArray flightnumbers = jsob.getJSONArray("flightnumbers");
                        for (int i = 0; i < flightnumbers.length(); i++) {
                            OutputStream outputStream = flights.getOutputStream();
                            JSONObject flightJson = new JSONObject();
                            flightJson.put("xid", jsob.getInt("xid"));
                            flightJson.put("customerId", jsob.getInt("customerId"));
                            flightJson.put("flightNumber", jsob.getInt(Integer.toString(i)));
                            outputStream.write(method.getBytes());
                            outputStream.flush();
                            outputStream.close();
                        }
                        if (jsob.getBoolean("car")) {
                            os = cars.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            roomObject.put("xid", jsob.getInt("xid"));
                            roomObject.put("customerId", jsob.getInt("customerId"));
                            roomObject.put("location", jsob.getString("location"));
                            os.write(roomObject.toString().getBytes());
                            os.flush();
                            os.close();
                            bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            os.write(js.getBytes());
                            os.flush();
                            os.close();
                        }
                        if (jsob.getBoolean("room")) {
                            os = rooms.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            roomObject.put("xid", jsob.getInt("xid"));
                            roomObject.put("customerId", jsob.getInt("customerId"));
                            roomObject.put("location", jsob.getString("location"));
                            os.write(roomObject.toString().getBytes());
                            os.flush();
                            os.close();
                            bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            os.write(js.getBytes());
                            os.flush();
                            os.close();
                        }

                    } else if (method.contains("Customer")) {
                        switch (method) {
                            case "AddCustomers":
                                int newId = middleware.newCustomer(jsob.getInt("xid"));
                                os = clientSocket.getOutputStream();
                                os.write(("Added customer with ID " + newId + ".").getBytes());
                                os.flush();
                                os.close();
                                break;
                            case "AddCustomerID":
                                boolean succesfullyCreated = middleware.newCustomer(jsob.getInt("xid"), jsob.getInt("customerID"));
                                os = clientSocket.getOutputStream();
                                if (succesfullyCreated) {
                                    os.write(("Added customer with ID " + jsob.getInt("customerID") + ".").getBytes());
                                } else {
                                    os.write(("Customer with ID " + jsob.getInt("customerID") + " already exists.").getBytes());
                                }
                                os.flush();
                                os.close();
                                break;
                            case "DeleteCustomer":
                                boolean succesfullyDeleted = middleware.newCustomer(jsob.getInt("xid"), jsob.getInt("customerID"));
                                os = clientSocket.getOutputStream();
                                if (succesfullyDeleted) {
                                    os.write(("Deleted customer with ID " + jsob.getInt("customerID") + ".").getBytes());
                                } else {
                                    os.write(("Customer with ID " + jsob.getInt("customerID") + " does not exist.").getBytes());
                                }
                                os.flush();
                                os.close();
                                break;
                        }
                    }
                } catch (Exception ioe) {
                    System.out.println("Exception encountered on accept. Ignoring. Stack Trace :");
                    ioe.printStackTrace();
                    try {
                        clientSocket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }
    }


