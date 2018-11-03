package Server.Common;

import java.rmi.RemoteException;
import java.util.*;
import Server.LockManager.*;

public class TransactionManager{

    protected HashMap<Integer, List<String>> activeTransactions;
    protected int nextTXVAL;
    protected List<Timer> TTL = new ArrayList<>();

    public TransactionManager()
    {
        activeTransactions = new HashMap<>();
        nextTXVAL = 0;
    }
    public int start() throws RemoteException {
        nextTXVAL++;
        List<String> list = new ArrayList<>();
        activeTransactions.put(nextTXVAL, list);
        System.out.println("activeTransactions" + activeTransactions);
        return nextTXVAL;
    }
    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        activeTransactions.remove(xid);
        TTL.remove(nextTXVAL);
        System.out.println("activeTransactions" + activeTransactions);
    }
    public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        return true;
    }
}