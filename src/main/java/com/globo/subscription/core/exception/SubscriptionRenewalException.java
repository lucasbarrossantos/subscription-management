package com.globo.subscription.core.exception;

public class SubscriptionRenewalException extends BusinessException {

    public SubscriptionRenewalException(String message) {
        super(message);
    }

    public SubscriptionRenewalException(String message, Throwable cause) {
        super(message, cause);
    }
}
