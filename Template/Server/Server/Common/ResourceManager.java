// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.*;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.rmi.RemoteException;

public class ResourceManager implements IResourceManager
{
	protected String m_name;
	public RMHashMap m_data;
	public File shadow_file_A;
	public File shadow_file_B;
	public File master_record;
	public File log;
	public String[] master_record_pointer;
	public int crash_mode;
	public HashMap<Integer, ArrayList<String>> live_log;
	protected static int DECISION_TIMEOUT = 50000;
	Timer crash_timer;
	public IResourceManager middleware;
	public HashMap<Integer, Timer> vote_req_timers;



	public ResourceManager(String p_name) {
		vote_req_timers = new HashMap<>();
		crash_mode = 0;
		m_name = p_name;
		m_data = new RMHashMap();
		//check if persistence exists, if so, get it!!
		shadow_file_A = new File("Persistence/" + p_name + "_shadow_file_A.ser");
		shadow_file_B = new File("Persistence/" + p_name + "_shadow_file_B.ser");
		master_record = new File("Persistence/" + p_name + "_master_record.ser");
		log = new File("Persistence/" + p_name + "_log.ser");
		try {
			shadow_file_A.createNewFile();
			shadow_file_B.createNewFile();
		} catch (Exception e) {
			System.out.println(e + "Trouble with file creating");
		}
		try {
		    if(!master_record.createNewFile()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(master_record));
                master_record_pointer = (String[]) ois.readObject();
            }
		} catch (Exception e) {
			System.out.println(e + "at master record reading.");
		}
		if(master_record_pointer == null) {
			master_record_pointer = new String[]{"",""};
		}
		// crash during recovery
		if(crash_mode == 5) {
			System.exit(1);
		}
		try {
		    if (!log.createNewFile()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(log));
                live_log = (HashMap<Integer, ArrayList<String>>) ois.readObject();
            }
		} catch(Exception e) {
			System.out.println(e + "at log reading.");
		}
		if(live_log == null) {
			live_log = new HashMap<>();
		}
		try {
			if (master_record_pointer[0].equals("shadow_file_A")) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(shadow_file_A));
				m_data = (RMHashMap) ois.readObject();
				System.out.println("Reading from file shadow_file_A.");
			} else if (master_record_pointer[0].equals("shadow_file_B")) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(shadow_file_B));
				m_data = (RMHashMap) ois.readObject();
				System.out.println("Reading from file shadow_file_B.");
			}
		} catch (Exception e) {
			System.out.println(e + " at file data");
		}
	}

	protected RMItem readData(int xid, String key)
	{
		synchronized(m_data) {
			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem)item.clone();
			}
			return null;
		}
	}
	// Writes a data item
	protected void writeData(int xid, String key, RMItem value)
	{
		synchronized(m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	protected void removeData(int xid, String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}

	public RMHashMap getData() {
		return (RMHashMap) m_data.clone();
	}

	// Deletes the encar item
	protected boolean deleteItem(int xid, String key)
	{
		Trace.info("RM::deleteItem(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		// Check if there is such an item in the storage
		if (curObj == null)
		{
			Trace.warn("RM::deleteItem(" + xid + ", " + key + ") failed--item doesn't exist");
			return false;
		}
		else
		{
			if (curObj.getReserved() == 0)
			{
				removeData(xid, curObj.getKey());
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
				return true;
			}
			else
			{
				Trace.info("RM::deleteItem(" + xid + ", " + key + ") item can't be deleted because some customers have reserved it");
				return false;
			}
		}
	}

	// Query the number of available seats/rooms/cars
	protected int queryNum(int xid, String key)
	{
		Trace.info("RM::queryNum(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0;  
		if (curObj != null)
		{
			value = curObj.getCount();
		}
		Trace.info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
		return value;
	}    

	// Query the price of an item
	protected int queryPrice(int xid, String key)
	{
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(xid, key);
		int value = 0; 
		if (curObj != null)
		{
			value = curObj.getPrice();
		}
		Trace.info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
		return value;        
	}

	// Reserve an item
	protected boolean reserveItem(int xid, int customerID, String key, String location)

	{
		Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );        
		// Read customer object if it exists (and read lock it)
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		} 

		// Check if the item is available
		ReservableItem item = (ReservableItem)readData(xid, key);
		if (item == null)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
			return false;
		}
		else if (item.getCount() == 0)
		{
			Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--No more items");
			return false;
		}
		else
		{            
			customer.reserve(key, location, item.getPrice());        
			writeData(xid, customer.getKey(), customer);

			// Decrease the number of available items in the storage
			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			writeData(xid, item.getKey(), item);

			Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
			return true;
		}        
	}

	// Create a new flight, or add seats to existing flight
	// NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException
	{
		Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
		Flight curObj = (Flight)readData(xid, Flight.getKey(flightNum));
		if (curObj == null)
		{
			// Doesn't exist yet, add it
			Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addFlight(" + xid + ") created new flight " + flightNum + ", seats=" + flightSeats + ", price=$" + flightPrice);
		}
		else
		{
			// Add seats to existing flight and update the price if greater than zero
			curObj.setCount(curObj.getCount() + flightSeats);
			if (flightPrice > 0)
			{
				curObj.setPrice(flightPrice);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addFlight(" + xid + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice);
		}
		return true;
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		Car curObj = (Car)readData(xid, Car.getKey(location));
		if (curObj == null)
		{
			// Car location doesn't exist yet, add it
			Car newObj = new Car(location, count, price);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addCars(" + xid + ") created new location " + location + ", count=" + count + ", price=$" + price);
		}
		else
		{
			// Add count to existing car location and update price if greater than zero
			curObj.setCount(curObj.getCount() + count);
			if (price > 0)
			{
				curObj.setPrice(price);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addCars(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
		}
		return true;
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		Trace.info("RM::addRooms(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
		Room curObj = (Room)readData(xid, Room.getKey(location));
		if (curObj == null)
		{
			// Room location doesn't exist yet, add it
			Room newObj = new Room(location, count, price);
			writeData(xid, newObj.getKey(), newObj);
			Trace.info("RM::addRooms(" + xid + ") created new room location " + location + ", count=" + count + ", price=$" + price);
		} else {
			// Add count to existing object and update price if greater than zero
			curObj.setCount(curObj.getCount() + count);
			if (price > 0)
			{
				curObj.setPrice(price);
			}
			writeData(xid, curObj.getKey(), curObj);
			Trace.info("RM::addRooms(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
		}
		return true;
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum)
			throws RemoteException , InvalidTransactionException, TransactionAbortedException
	{
		return deleteItem(xid, Flight.getKey(flightNum));
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return deleteItem(xid, Car.getKey(location));
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location)
			throws RemoteException, InvalidTransactionException, TransactionAbortedException
	{
		return deleteItem(xid, Room.getKey(location));
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return queryNum(xid, Flight.getKey(flightNum));
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return queryNum(xid, Car.getKey(location));

	}
	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return queryNum(xid, Room.getKey(location));
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return queryPrice(xid, Flight.getKey(flightNum));
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return queryPrice(xid, Car.getKey(location));
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return queryPrice(xid, Room.getKey(location));
	}

	public String queryCustomerInfo(int xid, int customerID)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::queryCustomerInfo(" + xid + ", " + customerID + ") failed--customer doesn't exist");
			// NOTE: don't change this--WC counts on this value indicating a customer does not exist...
			return "";
		}
		else
		{
			Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ")");
			System.out.println(customer.getBill());
			return customer.getBill();
		}
	}

	public int newCustomer(int xid)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
        	Trace.info("RM::newCustomer(" + xid + ") called");
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt(String.valueOf(xid) +
			String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
			String.valueOf(Math.round(Math.random() * 100 + 1)));
		Customer customer = new Customer(cid);
		writeData(xid, customer.getKey(), customer);
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
		return cid;
	}

	public boolean newCustomer(int xid, int customerID)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			customer = new Customer(customerID);
			writeData(xid, customer.getKey(), customer);
			Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") created a new customer");
			return true;
		}
		else
		{
			Trace.info("INFO: RM::newCustomer(" + xid + ", " + customerID + ") failed--customer already exists");
			return false;
		}
	}

	public boolean deleteCustomer(int xid, int customerID)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
		Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
			return false;
		}
		else
		{            
			// Increase the reserved numbers of all reservable items which the customer reserved. 
 			RMHashMap reservations = customer.getReservations();
			for (String reservedKey : reservations.keySet())
			{        
				ReservedItem reserveditem = customer.getReservedItem(reservedKey);
				Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
				ReservableItem item  = (ReservableItem)readData(xid, reserveditem.getKey());
				Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
				item.setReserved(item.getReserved() - reserveditem.getCount());
				item.setCount(item.getCount() + reserveditem.getCount());
				writeData(xid, item.getKey(), item);
			}

			// Remove the customer from the storage
			removeData(xid, customer.getKey());
			Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
			return true;
		}
	}

	// Adds flight reservation to this customer
	public boolean reserveFlight(int xid, int customerID, int flightNum)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return reserveItem(xid, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
	}

	// Adds car reservation to this customer
	public boolean reserveCar(int xid, int customerID, String location)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return reserveItem(xid, customerID, Car.getKey(location), location);
	}

	// Adds room reservation to this customer
	public boolean reserveRoom(int xid, int customerID, String location)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return reserveItem(xid, customerID, Room.getKey(location), location);
	}

	// Reserve bundle 
	public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room)
			throws RemoteException,  InvalidTransactionException, TransactionAbortedException
	{
		return false;
	}

	public String getName() throws RemoteException
	{
		return m_name;
	}

	public int start() throws RemoteException{

		return 1;
	}

	public boolean commit(int xid) throws RemoteException,
			TransactionAbortedException, InvalidTransactionException {
		if(live_log.get(xid) == null || live_log.get(xid).contains("COMMIT") || live_log.get(xid).contains("ABORT")) {
			System.out.println(m_name + " is voting no.");
			vote(xid,0);
			return false;
		}
		String s = "Committing transaction # " + xid + " to file " + m_name + "_" + master_record_pointer[0] + ".";
		live_log.get(xid).add("COMMIT");
		// crash after receiving decision but before committing/aborting
		if(crash_mode == 4) {
			System.exit(1);
		}
		try {
			ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream("Persistence/" + m_name + "_master_record.ser"));
			ois.writeObject(master_record_pointer);
			ois = new ObjectOutputStream(new FileOutputStream("Persistence/" + m_name + "_log.ser"));
			ois.writeObject(live_log);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		System.out.println(s);
		return true;
	}

	public void abort(int xid) throws RemoteException,
			InvalidTransactionException{
		if(live_log.get(xid) == null || live_log.get(xid).contains("ABORT")) {
			return;
		}
		//write to log, and then reread master record
		// crash after receiving decision but before committing/aborting
		if(crash_mode == 4) {
			System.exit(1);
		}
		if(live_log.get(xid) == null) {
			ArrayList<String> list = new ArrayList<>();
			list.add("ABORT");
			live_log.put(xid,list);
		}
		else {
			live_log.get(xid).add("ABORT");
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(master_record));
			master_record_pointer = (String[]) ois.readObject();
		}
		catch (Exception e) {
			System.out.println(e + " while reading master record");
		}
		try {
			if (master_record_pointer[0].equals("shadow_file_A")) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(shadow_file_A));
				m_data = (RMHashMap) ois.readObject();
				System.out.println("Undoing by reading from file " + master_record_pointer[0]);
			} else if (master_record_pointer[0].equals("shadow_file_B")) {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(shadow_file_B));
				m_data = (RMHashMap) ois.readObject();
				System.out.println("Undoing by reading from file " + master_record_pointer[0]);
			} else {
				m_data = new RMHashMap();
			}
		}
			catch(Exception e) {
				System.out.println(e + " while aborting " + xid );
			}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(master_record));
			master_record_pointer = (String[])ois.readObject();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Persistence/" + m_name + "_log.ser"));
			oos.writeObject(live_log);

		} catch (Exception e) {
			System.out.println(e + " at master record reading.");
		}
		if (master_record_pointer == null) {
			master_record_pointer = new String[]{"", ""};
		}
		System.out.println("Aborting transaction " + xid + ".");
	}

	public void prepare(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
		if(live_log.get(xid) == null || live_log.get(xid).contains("COMMIT") || live_log.get(xid).contains("ABORT")) {
			System.out.println(m_name + " is voting no.");
			vote(xid,0);
			return;
		}
		//first write main mem copy to NOT last committed version, then write master record, then say yes

		// crash after receiving vote request, but before sending answer
		if (crash_mode == 1) {
			System.exit(1);
		}
		ArrayList<String> list = new ArrayList<>();
		list.add("YES");
		live_log.put(xid, list);
		vote_req_timers.get(xid).cancel();
		if (master_record_pointer[0].equals("shadow_file_A")) {
			master_record_pointer[0] = "shadow_file_B";
		} else {
			master_record_pointer[0] = "shadow_file_A";
		}
		master_record_pointer[1] = Integer.toString(xid);
		System.out.println(master_record_pointer[0] + " " + master_record_pointer[1]);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Persistence/" + m_name + "_" + master_record_pointer[0] + ".ser"));
			oos.writeObject(m_data);
			System.out.println("Preparing to commit xid " + xid + " by writing to " + master_record_pointer[0]);
			oos = new ObjectOutputStream(new FileOutputStream("Persistence/" + m_name + "_log.ser"));
			oos.writeObject(live_log);
		} catch (Exception e) {
			System.out.println(e + "trying to write log");
		}
		// crash after decision which answer to send
		if (crash_mode == 2) {
			System.exit(1);
		}
        vote(xid,1);
		// crash after sending answer
        if (crash_mode == 3) {
            System.exit(1);
        }
    }


	public void resetCrashes() throws RemoteException {
		crash_mode = 0;
	}

	public void crashMiddleware(int mode) throws RemoteException {
	}

	public void crashResourceManager(String name /* RM Name */, int mode)
			throws RemoteException {
		crash_mode = mode;
	}
	public void vote(int xid, int decision) throws RemoteException {
		System.out.println(m_name + " votes " + decision + " on " + xid);
		try {
			middleware.vote(xid, decision);
		}
		catch(Exception e) {
			try {
				Registry registry = LocateRegistry.getRegistry("localhost", 1099);
				middleware = (IResourceManager) registry.lookup("group20Middleware");
				middleware.vote(xid, decision);
			}
			catch(Exception f) {
				System.out.println("Could not reconnect to middleware");
			}
		}
	}

	public void queryLog() throws RemoteException {
		System.out.println("Log for " + m_name);
		for (Integer i : live_log.keySet()) {
			System.out.print(i + " ");
			for (String j : live_log.get(i)) {
				System.out.print(j + " ");
			}
			System.out.println("\n");
		}
	}

	public boolean didCommit(int xid) {
		return false;
	}

	protected void recover() {
		for(Integer i : live_log.keySet()) {
			if(live_log.get(i).contains("YES") && !(live_log.get(i).contains("COMMIT") || live_log.get(i).contains("ABORT"))){
				try {
					if(middleware.didCommit(i)) {
						//need to read from OTHER data site...
						if (master_record_pointer[0].equals("shadow_file_A")) {
							master_record_pointer[0] = "shadow_file_B";
						} else {
							master_record_pointer[0] = "shadow_file_A";
						}
						master_record_pointer[1] = Integer.toString(i);
						ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Persistence/" + m_name + "_" + master_record_pointer[0] + ".ser"));
						m_data = (RMHashMap) ois.readObject();
						commit(i);
						System.out.println("Commiting transaction " + i + " upon recovery by reading from " + master_record_pointer[0]);
					}
					else{
						abort(i);
					}
				}
				catch(Exception e){
					System.out.println("at rm recovery....");

				}
				if(crash_mode == 5) {
					System.exit(1);
				}
			}
			else if (live_log.get(i).contains("COMMIT") && Integer.parseInt(master_record_pointer[1]) < i) {
				//only wrote commit but did not actually commit!!
				try {
					if (master_record_pointer[0].equals("shadow_file_A")) {
						master_record_pointer[0] = "shadow_file_B";
					} else {
						master_record_pointer[0] = "shadow_file_A";
					}
					master_record_pointer[1] = Integer.toString(i);
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Persistence/" + m_name + "_" + master_record_pointer[0] + ".ser"));
					m_data = (RMHashMap) ois.readObject();
					commit(i);
					System.out.println("Commiting transaction " + i + " upon recovery by reading from " + master_record_pointer[0]);
				}
				catch(Exception e) {
				}
			}
			else if (!live_log.get(i).contains("YES") && !live_log.get(i).contains("COMMIT") && !live_log.get(i).contains("ABORT")) {
				try {
					System.out.println("Aborting transaction " + i + " at recovery.");
					abort(i);
				}
				catch(Exception e){
					System.out.println("Could not abort " + i + " at recovery.");
				}
			}
		}
	}
	public void updateLog(int xid) {
		ArrayList<String> list = new ArrayList<>();
		live_log.put(xid,list);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Persistence/" + m_name + "_log.ser"));
			oos.writeObject(live_log);
		}
		catch(Exception e) {

		}
	}
	public void updateVoteReqTimer(int xid) {
		Timer t = new Timer();
		t.schedule(new

						   TimerTask() {
							   public void run() {
								   try {
									   System.out.println("Vote req timer timed out.");
									   abort(xid);
									   throw new TransactionAbortedException(xid);
								   } catch (Exception e) {
								   }
							   }
						   }, 40000);
		vote_req_timers.put(xid, t);
	}
}
 
