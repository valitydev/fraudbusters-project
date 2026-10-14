package dev.vality.fraudbusters.mg.connector.exception;

public class PaymentInfoNotFoundException extends RuntimeException {
    public PaymentInfoNotFoundException() {
    }

    public PaymentInfoNotFoundException(String message) {
        super(message);
    }

    public PaymentInfoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
