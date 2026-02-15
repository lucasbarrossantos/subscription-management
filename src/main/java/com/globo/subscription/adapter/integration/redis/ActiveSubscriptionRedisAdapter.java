package com.globo.subscription.adapter.integration.redis;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.port.out.subscription.ActiveSubscriptionCachePort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ActiveSubscriptionRedisAdapter implements ActiveSubscriptionCachePort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Value("${redis.cache.active-subscription-ttl-seconds:3600}")
    private long ttlSeconds;

    private String key(UUID userId) {
        return "active-subscription:" + userId;
    }

    @Override
    public void putActiveSubscription(UUID userId, Subscription subscription, long ttlSeconds) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key(userId), subscription, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<Subscription> getActiveSubscription(UUID userId) {

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Object value = ops.get(key(userId));

        if (value instanceof Subscription sub) {
            return Optional.of(sub);

        }

        Optional<Subscription> dbResult = subscriptionRepositoryPort.findActiveByUserId(userId);
        dbResult.ifPresent(sub -> putActiveSubscription(userId, sub, ttlSeconds));
        return dbResult;
    }

    @Override
    public void removeActiveSubscription(UUID userId) {
        redisTemplate.delete(key(userId));
    }
}