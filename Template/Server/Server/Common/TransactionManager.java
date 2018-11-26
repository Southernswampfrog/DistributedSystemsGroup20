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
    protected static int TTL_TIMEOUT = 50000;
    protected LockManager lm;
    public int crash_mode = 0;
    public HashMap<Integer, ArrayList<String>> live_log;
    public File log;
    public Map<Integer, ArrayList<Integer>> votes;

    public TransactionManager(LockManager lockmanager) {
        votes = new HashMap<>();
        log = new File("Persistence/TM_log.ser");
        try {
            if (!log.createNewFile()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(log));
                live_log = (HashMap<Integer, ArrayList<String>>) ois.readObject();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        if (live_log == null) {
            live_log = new HashMap<>();
        }
        activeTransactions = new HashMap<>();
        next_xid = 0;
        lm = lockmanager;
        votes = new HashMap<>();
    }

    public synchronized int start() {
        for (Integer i : live_log.keySet()) {
            next_xid = Math.max(i, next_xid);
        }
        next_xid++;
        activeTransactions.put(next_xid, new HashSet<>());
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
        if(live_log.get(xid) == null) {
            ArrayList<String> list = new ArrayList<>();
            list.add("ABORT");
            live_log.put(xid,list);
        }
        else {
            live_log.get(xid).add("ABORT");
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(log));
            oos.writeObject(live_log);
        } catch (Exception e) {
            System.out.println(e + "cannot write TM log while aborting");
        }
        Iterator<IResourceManager> irm = activeTransactions.get(xid).iterator();
        lm.UnlockAll(xid);
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        try {
            while (irm.hasNext()) {
                IResourceManager ir = irm.next();
                ir.abort(xid);
            }
        }
        catch(Exception e) {
            System.out.println("Error while attempting TM abort.");
        }
        activeTransactions.remove(xid);
        System.out.println("Transaction " + xid + " aborted.");
    }

    public synchronized boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {

        //Start 2PC
        ArrayList<String> list   = new ArrayList<>();
        list.add("START");
        live_log.put(xid, list);
        votes.put(xid,new ArrayList<>());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(log));
            oos.writeObject(live_log);
        } catch (Exception e) {
            System.out.println("Cannot write TM log at committing");
        }
        if (crash_mode == 1) {
            System.exit(1);
        }


        //send VOTE-REQ to participants
        Iterator<IResourceManager> irm = activeTransactions.get(xid).iterator();
        while (irm.hasNext()) {
            try {
                irm.next().prepare(xid);
            }
            catch(Exception e) {
                e.printStackTrace();
                System.out.println("Error while sending vote-req to participants.");

            }
        }
        if (crash_mode == 2) {
            System.exit(1);
        }

        //wait for answers on vote-req
        int j = 0;
        try {
            while (votes.get(xid).size() < activeTransactions.get(xid).size() && j++ < 100) {
                Thread.sleep(100);
                if (crash_mode == 3) {
                    System.exit(1);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (j >= 99) {
            System.out.println("Coordinator timed out on vote-req.");
            abort(xid);
            return false;
        }
        if (crash_mode == 4) {
            System.exit(1);
        }

        //all votes are in, see if can commit or not
        for(Integer i : votes.get(xid)) {
            //transaction can't commit
            if (i == 0) {
                abort(xid);
                return false;
            }
        }
        System.out.println("Transaction " + xid + " can commit.");


        //write COMMIT to log, send commit to all participants
        live_log.get(xid).add("COMMIT");
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(log));
            oos.writeObject(live_log);
        } catch (Exception e) {
            System.out.println("Cannot write TM log at committing");
        }
        if (crash_mode == 5) {
            System.exit(1);
        }
        try {
            irm = activeTransactions.get(xid).iterator();
            while (irm.hasNext()) {
                IResourceManager ir = irm.next();
                ir.commit(xid);
                System.out.println(ir.getName() + " committed.");
                if (crash_mode == 6) {
                    System.exit(1);
                }
            }
        }
        catch(Exception e) {
            System.out.println("error at sending commit to participants");
        }

        activeTransactions.remove(xid);
        lm.UnlockAll(xid);
        TTLMap.get(xid).cancel();
        TTLMap.remove(xid);
        if (crash_mode == 7) {
            System.exit(1);
        }
        return true;
    }

    public void queryLog() {
        System.out.println("TM log:");
        for (Integer i : live_log.keySet()) {
            System.out.print(i + " ");
            for (String j : live_log.get(i)) {
                System.out.print(j + " ");
            }
            System.out.println("\n");
        }
    }
}