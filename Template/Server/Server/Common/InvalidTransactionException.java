package Server.Common;

public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(int xid) {
        super("the transaction " + xid + " is invalid");
    }
}
