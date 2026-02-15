package com.globo.subscription.adapter.integration.redis;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ActiveSubscriptionRedisAdapterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @InjectMocks
    private ActiveSubscriptionRedisAdapter adapter;

    private UUID userId;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        subscription = new Subscription();
    }

    @Test
    void putActiveSubscription_shouldStoreInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        adapter.putActiveSubscription(userId, subscription, 3600);
        verify(valueOperations).set(eq("active-subscription:" + userId), eq(subscription), eq(3600L), eq(java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void getActiveSubscription_shouldReturnFromRedisIfExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("active-subscription:" + userId)).thenReturn(subscription);
        Optional<Subscription> result = adapter.getActiveSubscription(userId);
        assertThat(result).isPresent().contains(subscription);
        verify(subscriptionRepositoryPort, never()).findActiveByUserId(any());
    }

    @Test
    void getActiveSubscription_shouldQueryDbAndCacheIfNotInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("active-subscription:" + userId)).thenReturn(null);
        when(subscriptionRepositoryPort.findActiveByUserId(userId)).thenReturn(Optional.of(subscription));
        doNothing().when(valueOperations).set(any(), any(), anyLong(), any());
        Optional<Subscription> result = adapter.getActiveSubscription(userId);
        assertThat(result).isPresent().contains(subscription);
        verify(subscriptionRepositoryPort).findActiveByUserId(userId);
        verify(valueOperations).set(eq("active-subscription:" + userId), eq(subscription), anyLong(), eq(java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void getActiveSubscription_shouldReturnEmptyIfNotInRedisOrDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("active-subscription:" + userId)).thenReturn(null);
        when(subscriptionRepositoryPort.findActiveByUserId(userId)).thenReturn(Optional.empty());
        Optional<Subscription> result = adapter.getActiveSubscription(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void removeActiveSubscription_shouldDeleteFromRedis() {
        adapter.removeActiveSubscription(userId);
        verify(redisTemplate).delete("active-subscription:" + userId);
    }
}
