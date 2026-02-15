package com.globo.subscription.core.exception;

public class SubscriptionAlreadyUpdatedException extends RuntimeException {
    public SubscriptionAlreadyUpdatedException(String message) {
        super(message);
    }
}
