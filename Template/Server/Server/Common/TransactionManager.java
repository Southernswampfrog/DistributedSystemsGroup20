package Server.Common;
import Server.Interface.IResourceManager;

import java.io.*;
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
    public ArrayList<String> live_log;
    public File log;

    public TransactionManager(LockManager lockmanager) {
        log = new File("Persistence/TM_log.ser");
        try {
            if (!log.createNewFile()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(log));
                live_log = (ArrayList<String>)ois.readObject();
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
        if(live_log == null) {
            live_log = new ArrayList<>();
        }
        activeTransactions = new HashMap<>();
        next_xid = 0;
        lm = lockmanager;
        undo = new HashMap<>();
        undoData = new HashMap<>();
    }

    public synchronized int start() {
        for (String i : live_log){
            int max = Character.getNumericValue(i.charAt(i.length() - 1));
            next_xid = Math.max(max, next_xid);
        }
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
                } catch (Exception e) {
                    System.out.println(e + " at timeout exception of" + transactionNum);
                }
            }
        }, TTL_TIMEOUT);
        TTLMap.put(next_xid, t);
        return next_xid;
    }

    public synchronized void abort(int xid) throws RemoteException, InvalidTransactionException {
        live_log.add("ABORT " + xid);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(log));
            oos.writeObject(live_log);
        }
        catch(Exception e) {
            System.out.println("cannot write TM log while aborting");
        }
        Iterator<IResourceManager> irm = activeTransactions.get(xid).iterator();
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
        while(irm.hasNext()) {
            IResourceManager ir = irm.next();
            ir.abort(xid);
        }
        activeTransactions.remove(xid);
    }

    public synchronized boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        live_log.add("START " + xid);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(log));
            oos.writeObject(live_log);
        }
        catch(Exception e) {
            System.out.println("Cannot write TM log while committing");
        }
        Iterator<IResourceManager> irm = activeTransactions.get(xid).iterator();
        if (crash_mode == 1) {
            System.exit(1);
        }
        while (irm.hasNext()) {
            IResourceManager ir = irm.next();
            ir.prepare(xid);
        }
        if (crash_mode == 2) {
            System.exit(1);
        }
        irm = activeTransactions.get(xid).iterator();
        while (irm.hasNext()) {
            IResourceManager ir = irm.next();
            boolean prepared = ir.getPrepare(xid);
            if (!(prepared)) {
                abort(xid);
                return false;
            }
            System.out.println(ir.getName() + " can commit.");
            if(crash_mode == 3) {
                System.exit(1);
            }
        }

        irm = activeTransactions.get(xid).iterator();
        if(crash_mode == 4) {
            System.exit(1);
        }
        live_log.add("COMMIT " + xid);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(log));
            oos.writeObject(live_log);
        }
        catch(Exception e) {
            System.out.println("Cannot write TM log while committing");
        }
        if(crash_mode == 5) {
            System.exit(1);
        }
        while(irm.hasNext()){
            IResourceManager ir = irm.next();
            boolean committed = ir.commit(xid);
            if (!committed) {
                return false;
            }
            System.out.println(ir.getName() + " committed.");
        }
        if(crash_mode == 7) {
            System.exit(1);
        }
        activeTransactions.remove(xid);
        lm.UnlockAll(xid);
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        undoData.remove(xid);
        undo.remove(xid);
        return true;
    }
    public void queryLog() {
        System.out.println("TM log:");
        for(String i : live_log) {
            System.out.println(i);
        }
    }
}