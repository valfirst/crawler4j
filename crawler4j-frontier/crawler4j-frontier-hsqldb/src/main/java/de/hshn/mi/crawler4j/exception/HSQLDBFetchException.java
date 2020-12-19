package de.hshn.mi.crawler4j.exception;

public class HSQLDBFetchException extends RuntimeException{

    private static final long serialVersionUID = 5328538498739884171L;

    public HSQLDBFetchException(String message) {
        super(message);
    }

    public HSQLDBFetchException(Throwable cause) {
        super(cause);
    }

    public HSQLDBFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
