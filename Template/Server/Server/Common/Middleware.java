// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;

public class Middleware extends ResourceManager implements IResourceManager
{
	protected String m_name = "";
	protected String[] m_RMNames = {};
	protected IResourceManager[] m_RMs = new IResourceManager[3];
	protected RMHashMap m_data = new RMHashMap();

	public Middleware(String p_name)
	{
        super(p_name);
        m_name = p_name;
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{
		return m_RMs[0].addFlight(xid, flightNum, flightSeats, flightPrice);
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price) throws RemoteException
	{
		return m_RMs[1].addCars(xid, location, count, price);
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
	{
		return m_RMs[2].addRooms(xid, location, count, price);
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum) throws RemoteException
	{
        return m_RMs[0].deleteFlight(xid, flightNum);
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location) throws RemoteException
	{
		return m_RMs[1].deleteCars(xid, location);
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location) throws RemoteException
	{
		return m_RMs[2].deleteRooms(xid, location);
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum) throws RemoteException
	{
		return m_RMs[0].queryFlight(xid, flightNum);
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location) throws RemoteException
	{
		return m_RMs[1].queryCars(xid, location);
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location) throws RemoteException
	{
		return m_RMs[2].queryRooms(xid, location);
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum) throws RemoteException
	{
		return m_RMs[0].queryFlightPrice(xid, flightNum);
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location) throws RemoteException
	{
		return m_RMs[1].queryCarsPrice(xid, location);
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location) throws RemoteException
	{
		return m_RMs[2].queryRoomsPrice(xid, location);
	}

	// Adds flight reservation to this customer
	public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
	{
		return m_RMs[0].reserveFlight(xid, customerID, flightNum);
	}

	// Adds car reservation to this customer
	public boolean reserveCar(int xid, int customerID, String location) throws RemoteException
	{
		return m_RMs[1].reserveCar(xid, customerID, location);
	}

	// Adds room reservation to this customer
	public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
	{
		return m_RMs[2].reserveRoom(xid, customerID, location);
	}

	public String queryCustomerInfo(int xid, int customerID) throws RemoteException
	{
		String x = "Bill for Customer " + customerID + ":";
		for(int i = 0; i < 3; i++) {
			x = x + m_RMs[i].queryCustomerInfo(xid,customerID);
		}
		x = x.replace("Bill for customer " + customerID, "");
		x = x.replace("\n", " ");
		return x;
	}

	public int newCustomer(int xid) throws RemoteException
	{
		int x;
		x = m_RMs[0].newCustomer(xid);
		m_RMs[1].newCustomer(xid,x);
		m_RMs[2].newCustomer(xid,x);
		return x;
	}

	public boolean newCustomer(int xid, int customerID) throws RemoteException
	{
		boolean x = false;
		for(int i = 0; i < 3; i++) {
			x = m_RMs[i].newCustomer(xid,customerID);
		}
		return x;
	}

	public boolean deleteCustomer(int xid, int customerID) throws RemoteException
	{
		boolean x = false;
		for(int i = 0; i < 3; i++) {
			x = m_RMs[i].deleteCustomer(xid,customerID);
		}
		return x;
	}

	// Adds flight reservation to this customer
	// Reserve bundle 
	public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{
		boolean x;
	    for (String flightNumber:flightNumbers){
	        x = m_RMs[0].reserveFlight(xid, customerId, Integer.parseInt(flightNumber));
	        if (!x) {
				return false;
			}
        }
        if (car) {
	        x = m_RMs[1].reserveCar(xid, customerId, location);
			if (!x){
				return false;
			}
        }
        if (room) {
	        x = m_RMs[2].reserveRoom(xid, customerId, location);
			if (!x){
				return false;
			}
        }
		return true;
	}

	public String getName() throws RemoteException
	{
		return m_name;
		}
}
 
