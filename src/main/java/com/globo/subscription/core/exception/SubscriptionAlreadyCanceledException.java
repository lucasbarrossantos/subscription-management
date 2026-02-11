package com.globo.subscription.core.exception;

public class SubscriptionAlreadyCanceledException extends RuntimeException {
    public SubscriptionAlreadyCanceledException(String message) {
        super(message);
    }
}
