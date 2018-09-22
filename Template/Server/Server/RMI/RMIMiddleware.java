// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.RMI;


import Server.Common.Middleware;
import Server.Interface.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIMiddleware //extends Middleware
{/*
	private static String[] s_RMNames = {"Servers"};
	private static String s_serverName = "Middleware";
	private static int s_serverPort = 1099;
	//TODO: REPLACE 'ALEX' WITH YOUR GROUP NUMBER TO COMPILE
	 private static String s_rmiPrefix = "group20";
    
	public static void main(String args[])
	{
		if (args.length > 0)
		{
			s_RMNames = args;
		}
			
		// Create the RMI server entry
		try {
			// Create a new Server object
			RMIMiddleware server = new RMIMiddleware(s_serverName);

			// Dynamically generate the stub (client proxy)
			IResourceManager resourceManager = (IResourceManager)UnicastRemoteObject.exportObject(server, 0);

			// Bind the remote object's stub in the registry
			Registry l_registry;
			try {
				l_registry = LocateRegistry.createRegistry(1099);
			} catch (RemoteException e) {
				l_registry = LocateRegistry.getRegistry(1099);
			}
			final Registry registry = l_registry;
			registry.rebind(s_rmiPrefix + s_serverName, resourceManager);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						registry.unbind(s_rmiPrefix + s_serverName);
						System.out.println("'" + s_serverName + "' middlerware unbound");
					}
					catch(Exception e) {
						System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
						e.printStackTrace();
					}
				}
			});
			System.out.println("'" + s_serverName + "' middleware server ready and bound to '" + s_rmiPrefix + s_serverName + "'");
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

    public void connectServer()
    {
        connectServer(s_RMNames, s_serverPort, s_serverName);
    }

    public void connectServer(String[] servers, int port, String name)
    {
        try {
            boolean first = true;
            while (true) {
                for (int i = 0; i < servers.length; i++) {
                    try {
                        Registry registry = LocateRegistry.getRegistry(servers[i], port);
                        m_RMs[i] = (IResourceManager) registry.lookup(s_rmiPrefix + servers[i]);
                        System.out.println("Connected to '" + servers[i] + "' server [" + servers[i] + ":" + port + "/" + s_rmiPrefix + name + "]");
                        break;
                    } catch (NotBoundException | RemoteException e) {
                        if (first) {
                            System.out.println("Waiting for '" + servers[i] + "' server [" + servers[i] + ":" + port + "/" + s_rmiPrefix + name + "]");
                            first = false;
                        }
                    }
                }
                Thread.sleep(500);
            }
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

	public RMIMiddleware(String name)
	{
		super(name);
	}*/
}
