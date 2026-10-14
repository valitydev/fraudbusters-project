package dev.vality.fraudbusters.management.exception;

public class SaveRowsException extends RuntimeException {

    public SaveRowsException() {
    }

    public SaveRowsException(String message) {
        super(message);
    }

    public SaveRowsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SaveRowsException(Throwable cause) {
        super(cause);
    }

    public SaveRowsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
