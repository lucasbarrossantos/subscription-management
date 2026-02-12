package com.globo.subscription.core.exception;

public class WalletTransactionException extends BusinessException {

    public WalletTransactionException(String message) {
        super(message);
    }

    public WalletTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
