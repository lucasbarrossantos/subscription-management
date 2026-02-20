package com.globo.subscription.core.usecase.subscription.strategy;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.TypePlan;

public interface PlanChangeStrategy {
    Subscription apply(Subscription existingSubscription, TypePlan newPlan, User user);
}