package Server.Common;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import Server.LockManager.*;

public class TransactionManager{

    protected HashMap<Integer, List<String>> activeTransactions;
    protected int nextTXVAL;
    protected List<Timer> TTL = new ArrayList<>();
    protected ResourceManager flights;
    protected Map<Integer, Runnable> undoMap = new HashMap<>();


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
        System.out.println(undoMap.values());
        activeTransactions.remove(xid);
        undoMap.get(xid).run();
        System.out.println("activeTransactions" + activeTransactions);
    }
    public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        if(activeTransactions.containsKey(xid)) {
            activeTransactions.remove(xid);
        }
        else {
            return false;
        }
        System.out.println("activeTransactions" + activeTransactions);
        return true;
    }
}