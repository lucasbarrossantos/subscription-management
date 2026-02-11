package com.globo.subscription.adapter.datasource.database.mapper;

import org.mapstruct.Mapper;

import com.globo.subscription.adapter.datasource.database.entity.SubscriptionEntity;
import com.globo.subscription.core.domain.Subscription;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionEntity toEntity(Subscription subscription);
    
    Subscription toDomain(SubscriptionEntity subscriptionEntity);
}
