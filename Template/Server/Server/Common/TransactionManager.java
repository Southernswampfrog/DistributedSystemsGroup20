package Server.Common;

import java.rmi.RemoteException;
import java.util.*;

import Server.LockManager.*;

public class TransactionManager {

    protected HashMap<Integer, Set<String>> activeTransactions;
    protected int next_xid;
    protected Map<Integer, Timer> TTLMap = new HashMap<>();
    protected RMHashMap[] m_RMs = new RMHashMap[3];
    protected static int TTL_TIMEOUT = 50000;
    protected LockManager lm;
    protected Runnable undo;


    public TransactionManager(LockManager lockmanager) {
        activeTransactions = new HashMap<>();
        next_xid = 0;
        lm = lockmanager;
    }

    public int start() throws RemoteException {
        next_xid++;
        Set<String> list = new HashSet<>();
        activeTransactions.put(next_xid, list);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    System.out.println("The transaction" + next_xid + "has timedout");
                    abort(next_xid);
                    throw new TransactionAbortedException(next_xid);
                } catch (Exception e) {
                }
            }
        }, TTL_TIMEOUT);
        TTLMap.put(next_xid, t);
        return next_xid;
    }

    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        activeTransactions.remove(xid);
        lm.UnlockAll(xid);
        undo.run();
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
    }

    public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        activeTransactions.remove(xid);
        lm.UnlockAll(xid);
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        return true;
    }
}