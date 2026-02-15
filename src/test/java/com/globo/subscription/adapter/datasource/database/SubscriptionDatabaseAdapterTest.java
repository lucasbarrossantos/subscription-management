package com.globo.subscription.adapter.datasource.database;

import com.globo.subscription.adapter.datasource.database.entity.SubscriptionEntity;
import com.globo.subscription.adapter.datasource.database.mapper.SubscriptionMapper;
import com.globo.subscription.adapter.datasource.database.repository.subscription.SubscriptionRepository;
import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubscriptionDatabaseAdapterTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionMapper subscriptionMapper;
    @InjectMocks
    private SubscriptionDatabaseAdapter adapter;

    private SubscriptionEntity entity;
    private Subscription domain;
    private UUID userId;
    private UUID subscriptionId;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();
        user = User.builder().id(userId).name("Test User").email("test@user.com").build();
        entity = new SubscriptionEntity();
        entity.setId(subscriptionId);
        domain = Subscription.builder().id(subscriptionId).user(user).status(SubscriptionStatus.ACTIVE).build();
    }

    @Test
    void save_shouldPersistAndReturnDomain() {
        when(subscriptionMapper.toEntity(domain)).thenReturn(entity);
        when(subscriptionRepository.save(entity)).thenReturn(entity);
        when(subscriptionMapper.toDomain(entity)).thenReturn(domain);
        Subscription result = adapter.save(domain);
        assertThat(result).isEqualTo(domain);
        verify(subscriptionRepository).save(entity);
    }

    @Test
    void findActiveByUserId_shouldReturnDomainIfExists() {
        when(subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.of(entity));
        when(subscriptionMapper.toDomain(entity)).thenReturn(domain);
        Optional<Subscription> result = adapter.findActiveByUserId(userId);
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    void findActiveByUserId_shouldReturnEmptyIfNotExists() {
        when(subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());
        Optional<Subscription> result = adapter.findActiveByUserId(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnDomainIfExists() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(entity));
        when(subscriptionMapper.toDomain(entity)).thenReturn(domain);
        Optional<Subscription> result = adapter.findById(subscriptionId);
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    void findById_shouldReturnEmptyIfNotExists() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());
        Optional<Subscription> result = adapter.findById(subscriptionId);
        assertThat(result).isEmpty();
    }

    @Test
    void findLatestByUserId_shouldReturnDomainIfExists() {
        when(subscriptionRepository.findFirstByUserIdOrderByStartDateDesc(userId)).thenReturn(Optional.of(entity));
        when(subscriptionMapper.toDomain(entity)).thenReturn(domain);
        Optional<Subscription> result = adapter.findLatestByUserId(userId);
        assertThat(result).isPresent().contains(domain);
    }

    @Test
    void findLatestByUserId_shouldReturnEmptyIfNotExists() {
        when(subscriptionRepository.findFirstByUserIdOrderByStartDateDesc(userId)).thenReturn(Optional.empty());
        Optional<Subscription> result = adapter.findLatestByUserId(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void findSubscriptionsToRenew_shouldReturnMappedList() {
        LocalDate date = LocalDate.now();
        List<SubscriptionEntity> entities = List.of(entity);
        List<Subscription> domains = List.of(domain);
        when(subscriptionRepository.findSubscriptionsToRenew(eq(date), any(PageRequest.class))).thenReturn(entities);
        when(subscriptionMapper.toDomain(entity)).thenReturn(domain);
        List<Subscription> result = adapter.findSubscriptionsToRenew(date, 10);
        assertThat(result).containsExactlyElementsOf(domains);
    }

    @Test
    void findSubscriptionsToRenew_shouldReturnEmptyList() {
        LocalDate date = LocalDate.now();
        when(subscriptionRepository.findSubscriptionsToRenew(eq(date), any(PageRequest.class))).thenReturn(Collections.emptyList());
        List<Subscription> result = adapter.findSubscriptionsToRenew(date, 10);
        assertThat(result).isEmpty();
    }
}
