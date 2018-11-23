package Server.Common;
import Server.Interface.IResourceManager;

import java.lang.module.ResolvedModule;
import java.rmi.RemoteException;
import java.util.*;

import Server.LockManager.*;

public class TransactionManager {

    protected HashMap<Integer, Set<IResourceManager>> activeTransactions;
    protected int next_xid;
    protected Map<Integer, Timer> TTLMap = new HashMap<>();
    protected Map<Integer, RMHashMap[]> undoData;
    protected static int TTL_TIMEOUT = 50000;
    protected LockManager lm;
    protected Map<Integer, ArrayList<Runnable>> undo;
    public int crash_mode = 0;


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
        int transactionNum = next_xid;
        t.schedule(new TimerTask() {
            public void run() {
                try {
                    abort(transactionNum);
                    throw new TransactionAbortedException(transactionNum);

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
        int size = undo.get(xid).size();
        ArrayList<Runnable> undoes = undo.get(xid);
        for (int i = 0; i < size; i++) {
            undoes.get(i).run();
        }
        undo.remove(xid);
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        undoData.remove(xid);
        undo.remove(xid);
    }

    public synchronized boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        Iterator<IResourceManager> irm = activeTransactions.get(xid).iterator();
        if (crash_mode == 1) {
            System.exit(1);
        }
        while (irm.hasNext()) {
            if (crash_mode == 2) {
                throw new RemoteException();
            }
            IResourceManager ir = irm.next();
            boolean prepared = ir.prepare(xid);
            if (!(prepared)) {
                abort(xid);
                return false;
            }
            System.out.println(ir.getName() + " can commit.");
        }
        irm = activeTransactions.get(xid).iterator();
        while(irm.hasNext()){
            IResourceManager ir = irm.next();
            boolean committed = ir.commit(xid);
            if (!committed) {
                return false;
            }
            System.out.println(ir.getName() + " committed.");
        }
        activeTransactions.remove(xid);
        lm.UnlockAll(xid);
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        undoData.remove(xid);
        undo.remove(xid);
        return true;
    }
}