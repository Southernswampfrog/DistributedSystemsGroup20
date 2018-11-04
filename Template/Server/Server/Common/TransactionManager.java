package Server.Common;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Callable;

import Server.Interface.IResourceManager;
import Server.LockManager.*;

public class TransactionManager{

    protected HashMap<Integer, List<String>> activeTransactions;
    protected int nextTXVAL;
    protected List<Timer> TTL = new ArrayList<>();
    protected Map<Integer, Runnable> commitMap = new HashMap<>();
    protected RMHashMap[] m_RMs = new RMHashMap[3];



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
        System.out.println("activeTransactions" + activeTransactions);
    }

    public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        if(activeTransactions.containsKey(xid)) {
            activeTransactions.remove(xid);
        }
        else {
            return false;
        }
        commitMap.get(xid).run();
        System.out.println("activeTransactions" + activeTransactions);
        return true;
    }
}