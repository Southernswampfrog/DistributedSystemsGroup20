// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.IResourceManager;
import Server.LockManager.*;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class Middleware extends ResourceManager implements IResourceManager  {
    protected String m_name = "";
    protected IResourceManager[] m_RMs = new IResourceManager[3];
    protected LockManager lm = new LockManager();
    protected TransactionManager tm = new TransactionManager(lm);
    protected static int TTL_TIMEOUT = 50000;
    protected List<String> dataToLock;
    protected HashMap<String, Integer> rmNameMap = new HashMap<>();

    public Middleware(String p_name) {
        super(p_name);
        m_name = p_name;
        dataToLock = new ArrayList<>();
        rmNameMap.put("flight", 0);
        rmNameMap.put("car", 1);
        rmNameMap.put("room",2);
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("flight-" + flightNum);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        int position = rmNameMap.get("flight");
        return m_RMs[position].addFlight(xid, flightNum, flightSeats, flightPrice);
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("car-"+location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[1].addCars(xid, location, count, price);

    }


    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("room-"+location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[2].addRooms(xid, location, count, price);
    }

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("flight-"+flightNum);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[0].deleteFlight(xid, flightNum);
    }


    // Delete cars at a location
    public boolean deleteCars(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("car-"+location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[1].deleteCars(xid, location);
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("room-"+location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[2].deleteRooms(xid, location);
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("flight-"+flightNum);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[0].queryFlight(xid, flightNum);
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("car-"+location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[1].queryCars(xid, location);
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("room-"+location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[2].queryRooms(xid, location);
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {

        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("flight-"+flightNum);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[0].queryFlightPrice(xid, flightNum);
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("car-"+location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[1].queryCarsPrice(xid, location);
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("room-"+location);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[2].queryRoomsPrice(xid, location);
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        dataToLock.add("flight-"+flightNum);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[0].reserveFlight(xid, customerID, flightNum);
    }

    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("car-"+location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[1].reserveCar(xid, customerID, location);
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("room"+location);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        return m_RMs[2].reserveRoom(xid, customerID, location);
    }

    public String queryCustomerInfo(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_READ;
        dataToLock.add("flightCust-"+customerID);
        dataToLock.add("carCust-"+customerID);
        dataToLock.add("roomCust-"+customerID);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        String x = "Bill for Customer " + customerID + ":";
        for (int i = 0; i < 3; i++) {
            x = x + m_RMs[i].queryCustomerInfo(xid, customerID);
        }
        x = x.replace("Bill for customer " + customerID, "");
        x = x.replace("\n", " ");
        dataToLock.clear();
        return x;
    }

    public int newCustomer(int xid)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        int x = m_RMs[0].newCustomer(xid);
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("flightCust-"+x);
        dataToLock.add("carCust-"+x);
        dataToLock.add("roomCust-"+x);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        m_RMs[1].newCustomer(xid, x);
        m_RMs[2].newCustomer(xid, x);
        dataToLock.clear();
        return x;
    }

    public boolean newCustomer(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;

        dataToLock.add("flightCust-"+customerID);
        dataToLock.add("carCust-"+customerID);
        dataToLock.add("roomCust-"+customerID);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        boolean x = false;
        for (int i = 0; i < 3; i++) {
            x = m_RMs[i].newCustomer(xid, customerID);
        }
        dataToLock.clear();
        return x;
    }

    public boolean deleteCustomer(int xid, int customerID)
            throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        dataToLock.add("flightCust-"+customerID);
        dataToLock.add("carCust-"+customerID);
        dataToLock.add("roomCust-"+customerID);
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        boolean x = false;
        for (int i = 0; i < 3; i++) {
            x = m_RMs[i].deleteCustomer(xid, customerID);
        }
        dataToLock.clear();
        return x;
    }

    // Adds flight reservation to this customer
    // Reserve bundle
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car,
                          boolean room) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        TransactionLockObject.LockType lockType = TransactionLockObject.LockType.LOCK_WRITE;
        for(String flightNumber : flightNumbers) {
            dataToLock.add("flight"+flightNumber);
        }
        if (car) {
            dataToLock.add("car"+location);
        }
        if (room) {
            dataToLock.add("room"+location);
        }
        ValidityAndLockCheck(dataToLock, xid, lockType);
        updateTM(dataToLock, xid, lockType);
        dataToLock.clear();
        //check before actually doing any writes
        int y;
        String custExist = (String)(new SubTransaction(() -> m_RMs[0].queryCustomerInfo(xid,customerId)).call());
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
        boolean m = true;
        boolean n = true;
        boolean o = true;
        for (String flightNumber : flightNumbers) {
            m = (boolean) new SubTransaction(() -> m_RMs[0].reserveFlight(xid, customerId, Integer.parseInt(flightNumber))).call();
        }
        if (car) {
            n = (boolean) new SubTransaction(() -> m_RMs[1].reserveCar(xid, customerId, location)).call();
        }
        if (room) {
            o = (boolean) new SubTransaction(() -> m_RMs[2].reserveRoom(xid, customerId, location)).call();
        }
        if (m && n && o) {
            return true;
        }
        else {
            return false;
        }
    }

    public int start() throws RemoteException {
        int xid = tm.start();
        System.out.println("Transaction " + xid + " started.");
        //get current state of RMs
        return xid;
    }

    public boolean commit(int xid) throws RemoteException,
            TransactionAbortedException, InvalidTransactionException {
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
        boolean x = false;
        try {
            x = tm.commit(xid);
        }
        catch (ConnectException e) {
            m_RMs[0] = m_RMs[1];
            rmNameMap.put("flight", 1);
            System.out.println(rmNameMap.values() + " " + rmNameMap.keySet());
        }
        return x;
    }

    public void abort(int xid) throws RemoteException,
            InvalidTransactionException {
        if (!tm.activeTransactions.containsKey(xid)) {
            throw new InvalidTransactionException(xid);
        }
        tm.abort(xid);
    }

    public void resetCrashes() throws RemoteException{
        tm.crash_mode = 0;
        for(int i = 0; i < m_RMs.length; i++) {
            m_RMs[i].resetCrashes();
        }
    }

    public void queryLog() throws RemoteException {
        for(int i = 0; i < m_RMs.length; i++) {
            m_RMs[i].queryLog();
        }
        tm.queryLog();
    }

    public void crashMiddleware(int mode) throws RemoteException {
        tm.crash_mode = mode;
    }

    public void crashResourceManager(String name /* RM Name */, int mode)
            throws RemoteException{
        for(int i = 0; i < m_RMs.length; i++) {
            if(name.equals(m_RMs[i].getName())){
                m_RMs[i].crashResourceManager(name,mode);
                System.out.println("crashing " +m_RMs[i].getName());
                return;
            }
        }
        System.out.println("No such RM exists");
    }

    public synchronized void updateTM(List<String> RMNames, int xid, TransactionLockObject.LockType locktype) throws RemoteException, ConcurrentModificationException {
        int position = 0;
            int length = RMNames.size();
            for (int i = 0; i < length; i++) {
                String j = RMNames.get(i);
                if (j.contains("flight")) {
                    position = rmNameMap.get("flight");
                } else if (j.contains("car")) {
                    position = rmNameMap.get("car");
                } else if (j.contains("room")) {
                    position = rmNameMap.get("room");
                }
                tm.activeTransactions.get(xid).add(m_RMs[position]);
                RMHashMap[] l = tm.undoData.get(xid);
                if (locktype == TransactionLockObject.LockType.LOCK_WRITE && l[position] == null) {
                    l[position] = m_RMs[position].getData();
                    tm.undoData.put(xid, l);
                    final int y = position;
                    //store method at Transaction Manager to recall this state
                    Runnable x = (() -> {
                        try {
                            m_RMs[y].abort(xid);
                        }
                        catch (Exception e) {
                            System.out.println(e + "at undo map.");
                            m_RMs[0] = m_RMs[1];
                            rmNameMap.put("flight", 1);
                            System.out.println(rmNameMap.values() + " " + rmNameMap.keySet());

                        }
                    });
                    if (tm.undo.containsKey(xid)) {
                        tm.undo.get(xid).add(x);
                    }
                }
            }
        //update timer
        Timer t = new Timer();
        t.schedule(new TimerTask() {
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
}
 
