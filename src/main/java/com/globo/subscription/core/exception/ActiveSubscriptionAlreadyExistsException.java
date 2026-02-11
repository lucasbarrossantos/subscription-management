package com.globo.subscription.core.exception;

public class ActiveSubscriptionAlreadyExistsException extends RuntimeException {
    public ActiveSubscriptionAlreadyExistsException(String message) {
        super(message);
    }
}
