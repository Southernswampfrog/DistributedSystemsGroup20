// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.IResourceManager;
import Server.LockManager.*;

import java.rmi.RemoteException;
import java.util.*;

public class Middleware extends ResourceManager implements IResourceManager {
    protected String m_name = "";
    protected IResourceManager[] m_RMs = new IResourceManager[3];
    protected LockManager lm = new LockManager();
    protected TransactionManager tm = new TransactionManager(lm);
    protected static int TTL_TIMEOUT = 50000;
    protected List<String> RMs;

    public Middleware(String p_name) {
        super(p_name);
        m_name = p_name;
        RMs = new ArrayList<>();
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        RMs.add("Flights");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[0].addFlight(xid, flightNum, flightSeats, flightPrice);
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        RMs.add("Cars");

        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[1].addCars(xid, location, count, price);

    }


    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Rooms");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[2].addRooms(xid, location, count, price);
    }

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Flights");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[0].deleteFlight(xid, flightNum);
    }


    // Delete cars at a location
    public boolean deleteCars(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Cars");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[1].deleteCars(xid, location);
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Rooms");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[2].deleteRooms(xid, location);
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        RMs.add("Flights");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[0].queryFlight(xid, flightNum);
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Cars");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[1].queryCars(xid, location);
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        RMs.add("Rooms");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[2].queryRooms(xid, location);
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {

        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        RMs.add("Flights");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[0].queryFlightPrice(xid, flightNum);
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Cars");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[1].queryCarsPrice(xid, location);
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Rooms");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[2].queryRoomsPrice(xid, location);
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        RMs.add("Flights");
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[0].reserveFlight(xid, customerID, flightNum);
    }

    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        RMs.add("Cars");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[1].reserveCar(xid, customerID, location);
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        RMs.add("Rooms");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        return m_RMs[2].reserveRoom(xid, customerID, location);
    }

    public String queryCustomerInfo(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        RMs.add("Flights");
        RMs.add("Cars");
        RMs.add("Rooms");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        String x = "Bill for Customer " + customerID + ":";
        for (int i = 0; i < 3; i++) {
            x = x + m_RMs[i].queryCustomerInfo(xid, customerID);
        }
        x = x.replace("Bill for customer " + customerID, "");
        x = x.replace("\n", " ");
        RMs.clear();
        return x;
    }

    public int newCustomer(int xid)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        RMs.add("Flights");
        RMs.add("Cars");
        RMs.add("Rooms");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        int x;
        x = m_RMs[0].newCustomer(xid);
        m_RMs[1].newCustomer(xid, x);
        m_RMs[2].newCustomer(xid, x);
        RMs.clear();
        return x;
    }

    public boolean newCustomer(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        RMs.add("Flights");
        RMs.add("Cars");
        RMs.add("Rooms");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        boolean x = false;
        for (int i = 0; i < 3; i++) {
            x = m_RMs[i].newCustomer(xid, customerID);
        }
        RMs.clear();
        return x;
    }

    public boolean deleteCustomer(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        RMs.add("Flights");
        RMs.add("Cars");
        RMs.add("Rooms");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        boolean x = false;
        for (int i = 0; i < 3; i++) {
            x = m_RMs[i].deleteCustomer(xid, customerID);
        }
        RMs.clear();
        return x;
    }

    // Adds flight reservation to this customer
    // Reserve bundle
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car,
                          boolean room) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        RMs.add("Flights");
        RMs.add("Cars");
        RMs.add("Rooms");
        ValidityAndLockCheck(RMs, xid, lockType);
        updateTM(RMs, xid);
        RMs.clear();
        boolean x;
        //check before actually doing any writes
        for(String flightNumber : flightNumbers) {
            int y = m_RMs[0].queryFlight(xid, Integer.parseInt(flightNumber));
            if (y < 1) {
                return false;
            }
            if(car) {
                y = m_RMs[1].queryCars(xid, location);
                if (y < 1) {
                    return false;
                }
            }
            if(room) {
                y = m_RMs[2].queryRooms(xid, location);
                if(y < 1) {
                    return false;
                }
            }
        }
        for (String flightNumber : flightNumbers) {
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

    public int start() throws RemoteException {
        int xid = tm.start();
        //get current state of RMs
        for (int i = 0; i < 3; i++) {
            tm.m_RMs[i] = m_RMs[i].getData();
        }
        //store method at Transaction Manager to recall this state
        Runnable x = (() -> {
            for (int i = 0; i < 3; i++) {
                try {
                    m_RMs[i] = (new ResourceManager(tm.m_RMs[i]));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
        tm.undo = x;
        return xid;
    }

    public boolean commit(int xid) throws RemoteException,
            TransactionAbortedException, InvalidTransactionException {
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
     //   lm.UnlockAll(xid);
        boolean x = tm.commit(xid);
        return x;
    }

    public void abort(int xid) throws RemoteException,
            InvalidTransactionException {
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
     //   lm.UnlockAll(xid);
        tm.abort(xid);

    }

    public boolean shutdown(int xid) throws RemoteException {
        lm.UnlockAll(xid);
        return true;
    }
    public void updateTM(List<String> RMNames, int xid) {
        for (String i : RMNames) {
            tm.activeTransactions.get(xid).add(i);
        }
        System.out.println("Active Transactions" + tm.activeTransactions);
        //update timer
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    System.out.println("The transaction" + xid + "has timed out");
                    tm.abort(xid);
                    throw new TransactionAbortedException(xid);
                }
                catch (Exception e) {
                }
            }
        }, TTL_TIMEOUT);
        tm.TTLMap.get(xid).cancel();
        tm.TTLMap.put(xid, t);
    }


    public void ValidityAndLockCheck(List<String> RMNames, int xid, TransactionLockObject.LockType locktype)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException{
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
        for (String i : RMNames)
        try {
            lm.Lock(xid, i, locktype);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid);
        }
    }
}
 
