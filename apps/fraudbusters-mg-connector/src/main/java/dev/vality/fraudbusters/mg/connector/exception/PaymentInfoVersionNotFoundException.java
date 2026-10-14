package dev.vality.fraudbusters.mg.connector.exception;

public class PaymentInfoVersionNotFoundException extends RuntimeException {
    public PaymentInfoVersionNotFoundException() {
    }

    public PaymentInfoVersionNotFoundException(String message) {
        super(message);
    }

    public PaymentInfoVersionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
