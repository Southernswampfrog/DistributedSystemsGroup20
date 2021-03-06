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
            Socket flights = new Socket(args[0], Integer.parseInt(args[1]));
            System.out.println("Connected to flights");
            Socket cars = new Socket(args[2], Integer.parseInt(args[3]));
            System.out.println("Connected to cars");
            Socket rooms = new Socket(args[4], Integer.parseInt(args[5]));
            System.out.println("Connected to rooms");
            ServerSocket server = new ServerSocket(8888);
            System.out.println("Server started at port 8888");


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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public TCPMiddleware(String name, IResourceManager rm1, IResourceManager rm2, IResourceManager rm3) {
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
            while (true) {
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
                    if (method.contains("Customer")) {
                        if (method.equals("AddCustomer")) {
                            int id = (jsob.getInt("id"));
                            OutputStream os1 = flights.getOutputStream();
                            OutputStream os2 = cars.getOutputStream();
                            OutputStream os3 = rooms.getOutputStream();
                            PrintWriter pw1 = new PrintWriter(os1);
                            PrintWriter pw2 = new PrintWriter(os2);
                            PrintWriter pw3 = new PrintWriter(os3);
                            pw1.println(jsob);
                            pw1.flush();
                            bis = new BufferedReader(new InputStreamReader(flights.getInputStream()));
                            js = bis.readLine();
                            JSONObject custid = new JSONObject();
                            try {
                                custid.put("id", id);
                                custid.put("customerID", Integer.parseInt(js));
                                custid.put("methodName", "AddCustomerID");
                                pw2.println(custid);
                                pw2.flush();
                                bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                                System.out.println(bis.readLine());
                                pw3.println(custid);
                                pw3.flush();
                                bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                                System.out.println(bis.readLine());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            os = clientSocket.getOutputStream();
                            PrintWriter pw = new PrintWriter(os);
                            pw.println("New Customer added with id " + js);
                            pw.flush();
                        } else if (method.equals("QueryCustomer")) {
                            String output = "";
                            int line;
                            OutputStream os1 = flights.getOutputStream();
                            OutputStream os2 = cars.getOutputStream();
                            OutputStream os3 = rooms.getOutputStream();
                            PrintWriter pw1 = new PrintWriter(os1);
                            PrintWriter pw2 = new PrintWriter(os2);
                            PrintWriter pw3 = new PrintWriter(os3);
                            pw1.println(jsob);
                            pw2.println(jsob);
                            pw3.println(jsob);
                            pw1.flush();
                            pw2.flush();
                            pw3.flush();
                            os = clientSocket.getOutputStream();
                            pw1 = new PrintWriter(os);
                            bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                            output = output + bis.readLine();
                            output = output + ", ";
                            bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                            output = output + bis.readLine();
                            output = output + ", ";
                            bis = new BufferedReader(new InputStreamReader(flights.getInputStream()));
                            output = output + bis.readLine();
                            output = output.replace("Bill for customer " + jsob.getInt("id"), "");
                            pw1.println(output);
                            pw1.flush();
                        } else {
                            OutputStream os1 = flights.getOutputStream();
                            OutputStream os2 = cars.getOutputStream();
                            OutputStream os3 = rooms.getOutputStream();
                            PrintWriter pw1 = new PrintWriter(os1);
                            PrintWriter pw2 = new PrintWriter(os2);
                            PrintWriter pw3 = new PrintWriter(os3);
                            pw1.println(jsob);
                            pw2.println(jsob);
                            pw3.println(jsob);
                            pw1.flush();
                            pw2.flush();
                            pw3.flush();
                            bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                            BufferedReader bis1 = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                            BufferedReader bis2 = new BufferedReader(new InputStreamReader(flights.getInputStream()));
                            js = bis1.readLine();
                            js = bis2.readLine();

                            //Do we need to check if customer was able to be added to all different RMs?
                            js = bis.readLine();

                            os = clientSocket.getOutputStream();
                            pw1 = new PrintWriter(os);
                            pw1.println(js);
                            pw1.flush();
                        }
                    } else if (method.contains("Flight")) {
                        redirectTo(js, flights);
                    } else if (method.contains("Car")) {
                        redirectTo(js, cars);
                    } else if (method.contains("Room")) {
                        redirectTo(js, rooms);
                    } else if (method.contains("Analytics")) {
                        PrintWriter pw;
                        if (method.contains("Flight")) {
                            os = flights.getOutputStream();
                            JSONObject flightObject = new JSONObject();
                            pw = new PrintWriter(os);
                            flightObject.put("methodName", jsob.getString("AnalyticsFlight"));
                            pw.print(flightObject);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(flights.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.println(js);
                            pw.flush();
                        } else if (method.contains("Car")) {
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
                        } else if (method.contains("Room")) {
                            os = rooms.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            pw = new PrintWriter(os);
                            roomObject.put("methodName", jsob.getString("AnalyticsRoom"));
                            pw.print(roomObject);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.println(js);
                            pw.flush();
                        } else if (method.contains("Customer")) {
                            os = rooms.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            pw = new PrintWriter(os);
                            roomObject.put("methodName", jsob.getString("AnalyticsCustomer"));
                            pw.print(roomObject);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                            js = bis.readLine();
                            os = clientSocket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.println(js);
                            pw.flush();
                        }
                    } else if (method.equals("bundle")) {
                        PrintWriter pw = null;
                        JSONArray flightnumbers = jsob.getJSONArray("flightNumbers");
                        for (int i = 0; i < flightnumbers.length(); i++) {
                            os = flights.getOutputStream();
                            JSONObject flightJson = new JSONObject();
                            flightJson.put("id", jsob.getInt("id"));
                            flightJson.put("customerID", jsob.getInt("customerID"));
                            flightJson.put("flightNum", flightnumbers.getInt(i));
                            flightJson.put("methodName", "ReserveFlight");
                            pw = new PrintWriter(os);
                            pw.println(flightJson);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(flights.getInputStream()));
                            js = bis.readLine();
                        }
                        if (jsob.getBoolean("car")) {
                            os = cars.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            pw = new PrintWriter(os);
                            roomObject.put("id", jsob.getInt("id"));
                            roomObject.put("customerID", jsob.getInt("customerID"));
                            roomObject.put("location", jsob.getString("location"));
                            roomObject.put("methodName", "ReserveCar");
                            pw.println(roomObject);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(cars.getInputStream()));
                            js = bis.readLine();
                        }
                        if (jsob.getBoolean("room")) {
                            os = rooms.getOutputStream();
                            JSONObject roomObject = new JSONObject();
                            pw = new PrintWriter(os);
                            roomObject.put("id", jsob.getInt("id"));
                            roomObject.put("customerID", jsob.getInt("customerID"));
                            roomObject.put("location", jsob.getString("location"));
                            roomObject.put("methodName", "ReserveRoom");
                            pw.println(roomObject);
                            pw.flush();
                            bis = new BufferedReader(new InputStreamReader(rooms.getInputStream()));
                            js = bis.readLine();
                        }
                        os = clientSocket.getOutputStream();
                        pw = new PrintWriter(os);
                        pw.println("Bundle Reserved");
                        pw.flush();
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


