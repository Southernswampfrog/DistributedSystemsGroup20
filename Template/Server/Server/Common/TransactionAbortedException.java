package Server.Common;

public class TransactionAbortedException extends Exception {
    public TransactionAbortedException(int xid)
    {
        super("The transaction " + xid + " has been aborted.");

    }
}

