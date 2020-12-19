package de.hshn.mi.crawler4j.exception;

public class HSQLDBStoreException extends RuntimeException{

    private static final long serialVersionUID = -6468344673441896423L;

    public HSQLDBStoreException(String message) {
        super(message);
    }

    public HSQLDBStoreException(Throwable cause) {
        super(cause);
    }

    public HSQLDBStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
