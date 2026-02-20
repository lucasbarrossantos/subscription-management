package com.globo.subscription.core.usecase.subscription.strategy;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.enums.TypePlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlanChangeStrategyResolver {

    private final UpgradePlanChangeStrategy upgradePlanChangeStrategy;
    private final DowngradePlanChangeStrategy downgradePlanChangeStrategy;
    private final ReactivationPlanChangeStrategy reactivationPlanChangeStrategy;
    private final NoChangePlanChangeStrategy noChangePlanChangeStrategy;

    public PlanChangeStrategy resolve(Subscription existingSubscription, TypePlan newPlan) {
        TypePlan oldPlan = existingSubscription.getPlan();
        int priceComparison = newPlan.getPrice().compareTo(oldPlan.getPrice());

        if (priceComparison == 0 && oldPlan.equals(newPlan)) {
            log.info("PlanChangeStrategyResolver.resolve - selecting ReactivationPlanChangeStrategy for subscriptionId={}, oldPlan={}, newPlan={}", existingSubscription.getId(), oldPlan, newPlan);
            return reactivationPlanChangeStrategy;
        }

        if (priceComparison > 0) {
            log.info("PlanChangeStrategyResolver.resolve - selecting UpgradePlanChangeStrategy for subscriptionId={}, oldPlan={}, newPlan={}", existingSubscription.getId(), oldPlan, newPlan);
            return upgradePlanChangeStrategy;
        }

        if (priceComparison < 0) {
            log.info("PlanChangeStrategyResolver.resolve - selecting DowngradePlanChangeStrategy for subscriptionId={}, oldPlan={}, newPlan={}", existingSubscription.getId(), oldPlan, newPlan);
            return downgradePlanChangeStrategy;
        }

        log.info("PlanChangeStrategyResolver.resolve - selecting NoChangePlanChangeStrategy for subscriptionId={}, oldPlan={}, newPlan={}", existingSubscription.getId(), oldPlan, newPlan);
        return noChangePlanChangeStrategy;
    }
}
