// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.RMI;

import Server.Interface.*;
import Server.Common.*;

import java.rmi.NotBoundException;
import java.util.*;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIResourceManager extends ResourceManager 
{
	private static String s_serverName = "Server";
	private static String s_rmiPrefix = "group20";
	private static String s_MiddleWareServer = "localhost";

	public static void main(String args[])
	{
		if (args.length > 0)
		{
			s_serverName = args[0];
		}

		if (args.length > 1)
		{
			s_MiddleWareServer = args[1];
		}
		// Create the RMI server entry
		try {
			// Create a new Server object
			Registry l_registry;
			try {
				l_registry = LocateRegistry.createRegistry(1099);
			} catch (RemoteException e) {
				l_registry = LocateRegistry.getRegistry(1099);
			}
			RMIResourceManager server = new RMIResourceManager(s_serverName, s_MiddleWareServer);
			// Dynamically generate the stub (client proxy)
			IResourceManager resourceManager = (IResourceManager)UnicastRemoteObject.exportObject(server, 0);

			// Bind the remote object's stub in the registry

			final Registry registry = l_registry;
			registry.rebind(s_rmiPrefix + s_serverName, resourceManager);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						registry.unbind(s_rmiPrefix + s_serverName);
						System.out.println("'" + s_serverName + "' resource manager unbound");
					}
					catch(Exception e) {
						System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
						e.printStackTrace();
					}
				}
			});                                       
			System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_rmiPrefix + s_serverName + "'");
			server.connectServer();
			server.recover();
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
		connectServer("Middleware",1099, s_serverName);
	}
	public void connectServer(String servers, int port, String name)
	{
		try {
			boolean first = true;
				while (true) {
					try {
						Registry registry = LocateRegistry.getRegistry(s_MiddleWareServer, port);
						middleware = (IResourceManager) registry.lookup("group20Middleware");
						System.out.println("Connected to '" + servers + "' server [" + servers + ":" + port + "/" + s_rmiPrefix + servers + "]");
						break;
					} catch (NotBoundException | RemoteException e) {
						if (first) {
							System.out.println("Waiting for '" + servers + "' server [" + servers+ ":" + port + "/" + servers+ "]");
							first = false;
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
	public RMIResourceManager(String name, String middleware)
	{
		super(name, middleware);
	}
}
