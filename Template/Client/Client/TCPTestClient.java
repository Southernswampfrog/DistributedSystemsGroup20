package Client;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

public class TCPTestClient extends TCPClient {
    public static void main(String[] args) {
        s_serverHost = args[0];
        s_serverPort = Integer.parseInt(args[1]);
        try {
            TCPTestClient tcpTestClient = new TCPTestClient();
            tcpTestClient.connectServer(s_serverHost, s_serverPort);
            tcpTestClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        // Prepare for reading commands
        System.out.println();
        System.out.println("Location \"help\" for list of supported commands");

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String[] testCases = {
                "AddCustomerID,2,2\r",
                "AddFlight,2,2,2,2\r",
                "ReserveFlight,2,2,2\r",
                "AddRooms,2,\"Italy\",2,2\r",
               // "ReserveRoom,2,2,\"Italy\"\r",
                "AddCars,2,\"Italy\",2,2\r",
                "ReserveCar,2,2,\"Italy\"\r",
                "QueryCustomer,2,2",
                "QueryCars,2,\"Italy\"\r",
                "quit\r"};
        for (String i : testCases) {
            // Read the next command
            String command = "";
            Vector<String> arguments = new Vector<String>();
            System.out.print((char) 27 + "[32;1m\n>] " + (char) 27 + "[0m");
            command = i;
            try {
                arguments = parse(command);
                Command cmd = Command.fromString((String) arguments.elementAt(0));
                try {
                    execute(cmd, arguments);
                } catch (Exception e) {
                    connectServer();
                    execute(cmd, arguments);
                }
            } catch (Exception e) {
                System.err.println((char) 27 + "[31;1mCommand exception: " + (char) 27 + "[0mUncaught exception");
                e.printStackTrace();
            }
        }
    }
}
