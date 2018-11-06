package Server.Common;

import java.rmi.RemoteException;
import java.util.*;

import Server.LockManager.*;

public class TransactionManager {

    protected HashMap<Integer, Set<String>> activeTransactions;
    protected int next_xid;
    protected Map<Integer, Timer> TTLMap = new HashMap<>();
    protected Map<Integer, RMHashMap[]> undoData = new HashMap<>();
    protected static int TTL_TIMEOUT = 100000;
    protected LockManager lm;
    protected Map<Integer, ArrayList<Runnable>> undo;


    public TransactionManager(LockManager lockmanager) {
        activeTransactions = new HashMap<>();
        next_xid = 0;
        lm = lockmanager;
        undo = new HashMap<>();
        undoData = new HashMap<>();
    }

    public synchronized int start() {
        next_xid++;
        activeTransactions.put(next_xid, new HashSet<>());
        undo.put(next_xid, new ArrayList<>());
        undoData.put(next_xid, new RMHashMap[3]);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    System.out.println("The transaction" + next_xid + "has timedout");
                    abort(next_xid);
                    throw new TransactionAbortedException(next_xid);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }, TTL_TIMEOUT);
        TTLMap.put(next_xid, t);
        return next_xid;
    }

    public synchronized void abort(int xid) throws RemoteException, InvalidTransactionException {
        activeTransactions.remove(xid);
        lm.UnlockAll(xid);
        for (Runnable i : undo.get(xid)) {
            i.run();
        }
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        undoData.remove(xid);
        undo.remove(xid);
    }

    public synchronized boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        activeTransactions.remove(xid);
        lm.UnlockAll(xid);
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        undoData.remove(xid);
        undo.remove(xid);
        return true;
    }
}