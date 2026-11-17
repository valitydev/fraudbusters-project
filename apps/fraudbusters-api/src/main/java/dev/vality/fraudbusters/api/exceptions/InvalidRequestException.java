package dev.vality.fraudbusters.api.exceptions;

/** A request the contract allows syntactically but fraudbusters cannot accept; answered with 400. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
