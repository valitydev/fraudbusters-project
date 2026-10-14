package dev.vality.fraudbusters.notificator.exception;

public class TelegramSendException extends RuntimeException {

    public TelegramSendException(String message) {
        super(message);
    }

    public TelegramSendException(String message, Throwable cause) {
        super(message, cause);
    }
}