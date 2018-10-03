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
                    if (method.contains("Reserve")) {
                        redirectTo(js, flights);
                    }

                    else if (method.contains("Flight")) {
                        redirectTo(js, flights);
                    } else if (method.contains("Car")) {
                        redirectTo(js, cars);
                    } else if (method.contains("Room")) {
                        redirectTo(js, rooms);
                    } else if (method.contains("Analytics")) {

                        os = flights.getOutputStream();
                        JSONObject flightObject = new JSONObject();
                        PrintWriter pw = new PrintWriter(os);
                        flightObject.put("methodName", jsob.getString("AnalyticsFlight"));
                        pw.print(flightObject);
                        pw.flush();
                        bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                        js = bis.readLine();
                        os = clientSocket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                        os = cars.getOutputStream();
                        JSONObject carObject = new JSONObject();
                        pw = new PrintWriter(os);
                        carObject.put("methodName", jsob.getString("AnalyticsCar"));
                        pw.print(carObject);
                        pw.flush();
                        bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                        js = bis.readLine();
                        os = clientSocket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                        os = rooms.getOutputStream();
                        JSONObject roomObject = new JSONObject();
                        pw = new PrintWriter(os);
                        roomObject.put("methodName", jsob.getString("AnalyticsRoom"));
                        pw.print(roomObject);
                        pw.flush();
                        bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                        js = bis.readLine();
                        os = clientSocket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.println(js);
                        pw.flush();
                    } else if (method.equals("bundle")) {
                        JSONArray flightnumbers = jsob.getJSONArray("flightnumbers");
                        for (int i = 0; i < flightnumbers.length(); i++) {
                            os = flights.getOutputStream();
                            JSONObject flightJson = new JSONObject();
                            flightJson.put("id", jsob.getInt("id"));
                            flightJson.put("customerId", jsob.getInt("customerId"));
                            flightJson.put("flightNumber", jsob.getInt(Integer.toString(i)));
                            PrintWriter pw = new PrintWriter(os);
                            pw.println(js);
                            pw.flush();
                        }
                        if (jsob.getBoolean("car")) {
                            os = cars.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            PrintWriter pw = new PrintWriter(os);
                            roomObject.put("id", jsob.getInt("id"));
                            roomObject.put("customerId", jsob.getInt("customerId"));
                            roomObject.put("location", jsob.getString("location"));
                            pw.print(roomObject);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.println(js);
                            pw.flush();
                        }
                        if (jsob.getBoolean("room")) {
                            os = rooms.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            PrintWriter pw = new PrintWriter(os);
                            roomObject.put("id", jsob.getInt("id"));
                            roomObject.put("customerId", jsob.getInt("customerId"));
                            roomObject.put("location", jsob.getString("location"));
                            pw.println(roomObject);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.println(js);
                            pw.flush();
                        }

                    } else if (method.contains("Customer")) {
                        switch (method) {
                            case "AddCustomer":
                                int newId = middleware.newCustomer(jsob.getInt("id"));
                                os = clientSocket.getOutputStream();
                                PrintWriter pw = new PrintWriter(os);
                                pw.println("Added customer with ID " + newId + ".");
                                pw.flush();
                                break;
                            case "QueryCustomer":
                            String customerInfo = middleware.queryCustomerInfo(jsob.getInt("id"), jsob.getInt("customerID"));
                            os = clientSocket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.println(customerInfo);
                            pw.flush();
                            break;
                            case "AddCustomerID":
                                boolean successfullyCreated = middleware.newCustomer(jsob.getInt("id"), jsob.getInt("customerID"));
                                os = clientSocket.getOutputStream();
                                pw = new PrintWriter(os);
                                if (successfullyCreated) {
                                    pw.println("Added customer with ID " + jsob.getInt("customerID") + ".");
                                } else {
                                    pw.println("Customer with ID " + jsob.getInt("customerID") + " already exists.");
                                }
                                pw.flush();
                                break;
                            case "DeleteCustomer":
                                boolean successfullyDeleted = middleware.newCustomer(jsob.getInt("id"), jsob.getInt("customerID"));
                                os = clientSocket.getOutputStream();
                                pw = new PrintWriter(os);
                                if (successfullyDeleted) {
                                    pw.println("Deleted customer with ID " + jsob.getInt("customerID") + ".");
                                } else {
                                    pw.println("Customer with ID " + jsob.getInt("customerID") + " does not exist.");
                                }
                                pw.flush();
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

        private void redirectTo(String js, Socket socket) throws IOException {
            OutputStream os;
            BufferedReader bis;
            os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.println(js);
            pw.flush();
            bis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            js = bis.readLine();
            os = clientSocket.getOutputStream();
            pw = new PrintWriter(os);
            pw.println(js);
            pw.flush();
        }
    }


