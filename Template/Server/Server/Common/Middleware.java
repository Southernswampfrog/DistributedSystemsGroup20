
package Server.Common;

import Server.Interface.IResourceManager;
import Server.LockManager.*;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class Middleware extends ResourceManager implements IResourceManager {
    private String m_name = "";
    protected IResourceManager[] m_RMs = new IResourceManager[3];
    private LockManager lm = new LockManager();
    protected TransactionManager tm;
    protected static int TTL_TIMEOUT = 50000;
    protected List<String> dataToLock;
    protected int serverport;
    protected String[] servers;

    public Middleware(String p_name, int port, String[] RM_servers) {
        super(p_name);
        m_name = p_name;
        serverport = port;
        servers = RM_servers;
        dataToLock = new ArrayList<>();
        tm = new TransactionManager(lm);
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("flight-" + flightNum);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[0].addFlight(xid, flightNum, flightSeats, flightPrice);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[0]);
            connectServer(servers[0], serverport, "Flights", 0);
            tm.activeTransactions.get(xid).add(m_RMs[0]);
            return m_RMs[0].addFlight(xid, flightNum, flightSeats, flightPrice);
        } catch (Exception e) {
            System.out.println(e + "Error at addFlights with xid " + xid);
            return false;
        }
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("car-" + location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[1].addCars(xid, location, count, price);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[1]);
            connectServer(servers[1], serverport, "Cars", 1);
            tm.activeTransactions.get(xid).add(m_RMs[1]);
            return m_RMs[1].addCars(xid, location, count, price);
        }
        catch (Exception e) {
            System.out.println("Error at addCars with xid " + xid);
            return false;
        }

    }


    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("room-" + location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[2].addRooms(xid, location, count, price);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[2]);
            connectServer(servers[2], serverport, "Rooms", 2);
            tm.activeTransactions.get(xid).add(m_RMs[2]);
            return m_RMs[2].addRooms(xid, location, count, price);
        }
        catch (Exception e){

        }
        return false;
    }

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("flight-" + flightNum);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[0].deleteFlight(xid, flightNum);
        }catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[0]);
            connectServer(servers[0], serverport, "Rooms", 0);
            tm.activeTransactions.get(xid).add(m_RMs[0]);
            return m_RMs[0].deleteFlight(xid, flightNum);
        } catch (Exception e) {
            System.out.println("Error at deleteFlight with xid " + xid);
            return false;
        }
    }


    // Delete cars at a location
    public boolean deleteCars(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("car-" + location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[1].deleteCars(xid, location);
        }
        catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[1]);
            connectServer(servers[1], serverport, "Cars", 1);
            tm.activeTransactions.get(xid).add(m_RMs[1]);
            return m_RMs[1].deleteCars(xid, location);
        }        catch (Exception e) {
            System.out.println("Error at deleteCars with xid " + xid);
            return false;
        }
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("room-" + location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[2].deleteRooms(xid, location);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[2]);
            connectServer(servers[2], serverport, "Rooms", 2);
            tm.activeTransactions.get(xid).add(m_RMs[2]);
            return m_RMs[2].deleteRooms(xid, location);
        }
        catch (Exception e) {
            System.out.println("Error at deleteRooms with xid" + xid);
            return false;
        }
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("flight-" + flightNum);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[0].queryFlight(xid, flightNum);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[0]);
            connectServer(servers[0], serverport, "Flights", 0);
            tm.activeTransactions.get(xid).add(m_RMs[0]);
            return m_RMs[0].queryFlight(xid, flightNum);
        }
        catch (Exception e) {
            return 0;
        }
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("car-" + location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[1].queryCars(xid, location);
        }catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[1]);
            connectServer(servers[1], serverport, "Cars", 1);
            tm.activeTransactions.get(xid).add(m_RMs[1]);
            return m_RMs[1].queryCars(xid, location);
        }
        catch (Exception e) {
            return 0;
        }
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("room-" + location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[2].queryRooms(xid, location);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[2]);
            connectServer(servers[2], serverport, "Rooms", 2);
            tm.activeTransactions.get(xid).add(m_RMs[2]);
            return m_RMs[2].queryRooms(xid, location);
        }
        catch (Exception e) {
            return 0;
        }
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {

        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("flight-" + flightNum);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[0].queryFlightPrice(xid, flightNum);
        }catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[0]);
            connectServer(servers[0], serverport, "Flights", 0);
            tm.activeTransactions.get(xid).add(m_RMs[0]);
            return m_RMs[0].queryFlightPrice(xid, flightNum);
        }
        catch (Exception e) {

        }
        return 0;
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("car-" + location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[1].queryCarsPrice(xid, location);
        }
        catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[1]);
            connectServer(servers[1], serverport, "Cars", 1);
            tm.activeTransactions.get(xid).add(m_RMs[1]);
            return m_RMs[1].queryCarsPrice(xid, location);
        }catch (Exception e) {
            return 0;
        }
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("room-" + location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[2].queryRoomsPrice(xid, location);
        }catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[2]);
            connectServer(servers[2], serverport, "Rooms", 2);
            tm.activeTransactions.get(xid).add(m_RMs[2]);
            return m_RMs[2].queryRoomsPrice(xid, location);
        } catch (Exception e) {
            System.out.println();
            return 0;
        }
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("flight-" + flightNum);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[0].reserveFlight(xid, customerID, flightNum);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[0]);
            connectServer(servers[0], serverport, "Flights", 0);
            tm.activeTransactions.get(xid).add(m_RMs[0]);
            return m_RMs[0].reserveFlight(xid, customerID, flightNum);
        }catch (Exception e) {
            System.out.println();
            return false;
        }
    }

    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("car-" + location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[1].reserveCar(xid, customerID, location);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[1]);
            connectServer(servers[1], serverport, "Cars", 1);
            tm.activeTransactions.get(xid).add(m_RMs[1]);
            return m_RMs[1].reserveCar(xid, customerID, location);
        }catch (Exception e) {
            System.out.println();
            return false;
        }
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("room" + location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            return m_RMs[2].reserveRoom(xid, customerID, location);
        } catch (ConnectException ce) {
            tm.activeTransactions.get(xid).remove(m_RMs[2]);
            connectServer(servers[2], serverport, "Rooms", 2);
            tm.activeTransactions.get(xid).add(m_RMs[2]);
            return m_RMs[2].reserveRoom(xid, customerID, location);
        }catch (Exception e) {
            return false;
        }
    }

    public String queryCustomerInfo(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("flightCust-" + customerID);
        dataToLock.add("carCust-" + customerID);
        dataToLock.add("roomCust-" + customerID);
        ValidityAndLockCheck(dataToLock, xid, lockType);
            updateTM(dataToLock, xid);
            String x = "Bill for Customer " + customerID + ":";
            for (int i = 0; i < 3; i++) {
                x = x + m_RMs[i].queryCustomerInfo(xid, customerID);
            }
            x = x.replace("Bill for customer " + customerID, "");
            x = x.replace("\n", " ");
            dataToLock.clear();
            return x;
    }

    public int newCustomer(int xid) {
        int x = 0;
        try {
            x = m_RMs[0].newCustomer(xid);
            TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
            dataToLock.add("flightCust-" + x);
            dataToLock.add("carCust-" + x);
            dataToLock.add("roomCust-" + x);
            ValidityAndLockCheck(dataToLock, xid, lockType);
            updateTM(dataToLock, xid);
            m_RMs[1].newCustomer(xid, x);
            m_RMs[2].newCustomer(xid, x);
            dataToLock.clear();
        } catch(ConnectException ce) {
            String[] servers1 = {"Flights", "Cars", "Rooms"};
            for (int i = 0; i < 3; i++) {
                tm.activeTransactions.get(xid).remove(m_RMs[i]);
                connectServer(servers[i], serverport, servers1[i], i);
                tm.activeTransactions.get(xid).add(m_RMs[i]);
            }
            try {
                x = m_RMs[0].newCustomer(xid);
                m_RMs[1].newCustomer(xid, x);
                m_RMs[2].newCustomer(xid, x);
            }
            catch(Exception e) {
            }
        }catch (Exception e) {
            System.out.println("Could not make new customer");
            x = 0;
        }
        return x;
    }

    public boolean newCustomer(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("flightCust-" + customerID);
        dataToLock.add("carCust-" + customerID);
        dataToLock.add("roomCust-" + customerID);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        boolean x = false;
        try {
            updateTM(dataToLock, xid);
            x = false;
            for (int i = 0; i < 3; i++) {
                x = m_RMs[i].newCustomer(xid, customerID);
            }
            dataToLock.clear();
        } catch(ConnectException ce){
            String[] servers1 = {"Flights", "Cars", "Rooms"};
            for(int i = 0; i < 3; i++) {
                tm.activeTransactions.get(xid).remove(m_RMs[i]);
                connectServer(servers[i], serverport, servers1[i], i);
                tm.activeTransactions.get(xid).add(m_RMs[i]);
            }
            for (int i = 0; i < 3; i++) {
                x = m_RMs[i].newCustomer(xid, customerID);
            }
        } catch(Exception e) {
            System.out.println("Could not create new customer.");
            x = false;
        }
        return x;
    }

    public boolean deleteCustomer(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("flightCust-" + customerID);
        dataToLock.add("carCust-" + customerID);
        dataToLock.add("roomCust-" + customerID);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        boolean x = false;
        try {
            updateTM(dataToLock, xid);
            x = false;
            for (int i = 0; i < 3; i++) {
                x = m_RMs[i].deleteCustomer(xid, customerID);
            }
            dataToLock.clear();
        } catch (ConnectException ce) {
            String[] servers1 = {"Flights", "Cars", "Rooms"};
            for (int i = 0; i < 3; i++) {
                tm.activeTransactions.get(xid).remove(m_RMs[i]);
                connectServer(servers[i], serverport, servers1[i], i);
                tm.activeTransactions.get(xid).add(m_RMs[i]);
            }
            for (int i = 0; i < 3; i++) {
                x = m_RMs[i].deleteCustomer(xid, customerID);
            }
        } catch (Exception e) {
            return false;
        }
        return x;
    }

    // Adds flight reservation to this customer
    // Reserve bundle
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car,
                          boolean room) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        for (String flightNumber : flightNumbers) {
            dataToLock.add("flight" + flightNumber);
        }
        if (car) {
            dataToLock.add("car" + location);
        }
        if (room) {
            dataToLock.add("room" + location);
        }
        ValidityAndLockCheck(dataToLock, xid, lockType);
        try {
            updateTM(dataToLock, xid);
            dataToLock.clear();
            //check before actually doing any writes
            int y;
            String custExist = (String) (new SubTransaction(() -> m_RMs[0].queryCustomerInfo(xid, customerId)).call());
            if (custExist.equals("")) {
                return false;
            }
            for (String flightNumber : flightNumbers) {
                y = (int) new SubTransaction(() -> m_RMs[0].queryFlight(xid, Integer.parseInt(flightNumber))).call();
                if (y < 1) {
                    return false;
                }
            }
            int z = (int) new SubTransaction(() -> m_RMs[1].queryCars(xid, location)).call();
            int w = (int) new SubTransaction(() -> m_RMs[2].queryRooms(xid, location)).call();
            if (room && w < 1) {
                return false;
            }
            if (car && z < 1) {
                return false;
            }
            boolean reserveFlight = true;
            boolean reserveCar = true;
            boolean reserveRoom = true;
            for (String flightNumber : flightNumbers) {
                reserveFlight = (boolean) new SubTransaction(() -> m_RMs[0].reserveFlight(xid, customerId, Integer.parseInt(flightNumber))).call();
            }
            if (car) {
                reserveCar = (boolean) new SubTransaction(() -> m_RMs[1].reserveCar(xid, customerId, location)).call();
            }
            if (room) {
                reserveRoom = (boolean) new SubTransaction(() -> m_RMs[2].reserveRoom(xid, customerId, location)).call();
            }
            if (reserveFlight && reserveCar && reserveRoom) {
                return true;
            } else {
                return false;
            }
        } catch (ConnectException ce) {
            String[] servers1 = {"Flights", "Cars", "Rooms"};
            for (int i = 0; i < 3; i++) {
                tm.activeTransactions.get(xid).remove(m_RMs[i]);
                connectServer(servers[i], serverport, servers1[i], i);
                tm.activeTransactions.get(xid).add(m_RMs[i]);
            }
            bundle(xid, customerId, flightNumbers, location,car, room);

        } catch (Exception e) {
            System.out.println("Error at bundle");
            return false;
        }
        return false;
    }

    public int start() {
        int xid = tm.start();
        System.out.println("Transaction " + xid + " started.");
        return xid;
    }

    public boolean commit(int xid) throws InvalidTransactionException {
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
        try {
            return tm.commit(xid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void abort(int xid) throws RemoteException,
            InvalidTransactionException {
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
        tm.abort(xid);
    }

    public void resetCrashes() throws RemoteException {
        tm.crash_mode = 0;
        try {
            for (int i = 0; i < m_RMs.length; i++) {
                m_RMs[i].resetCrashes();
            }
        } catch (Exception e) {
            System.out.println("Could not reset crashes.");
        }
    }

    public void queryLog() throws RemoteException {
        for (int i = 0; i < m_RMs.length; i++) {
            m_RMs[i].queryLog();
        }
        try {
            tm.queryLog();
        } catch (Exception e) {
            System.out.println("Could not query log.");
        }
    }

    public void crashMiddleware(int mode) throws RemoteException {
        tm.crash_mode = mode;
    }

    public void crashResourceManager(String name /* RM Name */, int mode)
            throws RemoteException {
        for (int i = 0; i < m_RMs.length; i++) {
            if (name.equals(m_RMs[i].getName())) {
                m_RMs[i].crashResourceManager(name, mode);
                System.out.println("crashing " + m_RMs[i].getName());
                return;
            }
        }
        System.out.println("No such RM exists");
    }

    public synchronized void updateTM(List<String> RMNames, int xid)
            throws RemoteException {
        int position = 0;
        int length = RMNames.size();
        for (int i = 0; i < length; i++) {
            String j = RMNames.get(i);
            if (j.contains("flight")) {
                position = 0;
            } else if (j.contains("car")) {
                position = 1;
            } else if (j.contains("room")) {
                position = 2;
            }
            tm.activeTransactions.get(xid).add(m_RMs[position]);
            m_RMs[position].updateLog(xid);
        }

        //update timer
        Timer t = new Timer();
        t.schedule(new

                           TimerTask() {
                               public void run() {
                                   try {
                                       System.out.println("The transaction " + xid + " has timed out");
                                       abort(xid);
                                       throw new TransactionAbortedException(xid);
                                   } catch (Exception e) {
                                   }
                               }
                           }, TTL_TIMEOUT);
        if (tm.TTLMap.get(xid) != null) {
            tm.TTLMap.get(xid).cancel();
        }
        tm.TTLMap.put(xid, t);
    }


    public synchronized void ValidityAndLockCheck(List<String> RMNames, int xid, TransactionLockObject.LockType locktype)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
        int j = RMNames.size();
        for (int i = 0; i < j; i++) {
            try {
                lm.Lock(xid, RMNames.get(i), locktype);
            } catch (DeadlockException e) {
                abort(xid);
                throw new TransactionAbortedException(xid);
            }
        }
    }

    public class SubTransaction implements Callable {
        protected Callable method;
        protected Object x;
        protected int subID;

        public SubTransaction(Callable method) {
            this.method = method;
            this.subID = ThreadLocalRandom.current().nextInt(0, 100000);
        }

        public Object call() {
            System.out.println("Running subtransaction: " + subID);
            try {
                x = method.call();
            } catch (Exception e) {
                System.out.println(e);
            }
            return x;
        }

    }

    public void vote(int xid, int decision) {
        tm.votes.get(xid).add(decision);
    }


    public void connectServer(String server, int port, String name, int position) {
        try {
            try {
                Registry registry = LocateRegistry.getRegistry(server, port);
                m_RMs[position] = (IResourceManager) registry.lookup("group20" + name);
                System.out.println("Connected to " + name + " at " + server);
            } catch (NotBoundException | RemoteException e) {
                System.out.println("Cannot connect to " + name + " at " + server);
            }
        } catch (Exception e) {
            System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean didCommit(int xid) {
        System.out.println(tm.live_log.keySet() + " " + tm.live_log.values());
        if (tm.live_log.get(xid).isEmpty()) {
            return false;
        }
        return tm.live_log.get(xid).contains("COMMIT");
    }
}