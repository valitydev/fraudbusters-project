package dev.vality.trusted.tokens.exception;

import lombok.Getter;

@Getter
public class TransactionSavingException extends RuntimeException {

    private final int trxIndex;

    public TransactionSavingException(Throwable cause, int trxIndex) {
        super(cause);
        this.trxIndex = trxIndex;
    }
}
