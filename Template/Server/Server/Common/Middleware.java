// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Vector;

public class Middleware extends ResourceManager implements IResourceManager
{
	protected String m_name = "";
	protected String[] m_RMNames = {};
	protected IResourceManager[] m_RMs = {};
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

	// Reserve bundle 
	public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{
	    for (String flightNumber:flightNumbers){
	        m_RMs[0].reserveFlight(xid, customerId, Integer.parseInt(flightNumber));
        }
        if (car) {
	        m_RMs[1].reserveCar(xid, customerId, location);
        }
        if (room) {
	        m_RMs[2].reserveRoom(xid, customerId, location);
        }
		return true;
	}

	public String getName() throws RemoteException
	{
		return m_name;
		}
}
 
