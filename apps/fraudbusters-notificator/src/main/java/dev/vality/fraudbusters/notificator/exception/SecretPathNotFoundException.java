package dev.vality.fraudbusters.notificator.exception;

public class SecretPathNotFoundException extends RuntimeException {
    public SecretPathNotFoundException(String message) {
        super(message);
    }
}