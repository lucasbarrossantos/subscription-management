package com.globo.subscription.core.usecase.subscription;

import com.globo.subscription.core.domain.Subscription;
import com.globo.subscription.core.domain.User;
import com.globo.subscription.core.domain.enums.SubscriptionStatus;
import com.globo.subscription.core.domain.enums.TypePlan;
import com.globo.subscription.core.port.out.payment.PaymentPort;
import com.globo.subscription.core.port.out.subscription.SubscriptionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RenewSubscriptionsUseCaseTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock
    private PaymentPort paymentPort;
    @InjectMocks
    private RenewSubscriptionsUseCase useCase;

    private Subscription subscription;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@user.com");
        user.setName("Test User");
        subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
        subscription.setUser(user);
        subscription.setPlan(TypePlan.BASIC);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setRenewalAttempts(0);
    }

    @Test
    void execute_shouldRenewAllSubscriptionsSuccessfully() {
        List<Subscription> toRenew = List.of(subscription);
        when(subscriptionRepositoryPort.findSubscriptionsToRenew(any(LocalDate.class), anyInt())).thenReturn(toRenew);
        when(subscriptionRepositoryPort.save(any())).thenReturn(subscription);
        doNothing().when(paymentPort).debitAmount(any(), any(), any(), any());

        List<Subscription> renewed = useCase.execute();

        assertThat(renewed).containsExactly(subscription);
        verify(paymentPort).debitAmount(
                eq(user.getId()), 
                eq(TypePlan.BASIC.getPrice()), 
                eq("Renovação de " + TypePlan.BASIC.getDescription()), 
                eq(subscription.getId())
        );
        verify(subscriptionRepositoryPort).save(any());
    }

    @Test
    void execute_shouldHandlePaymentFailureAndIncrementAttempts() {
        List<Subscription> toRenew = List.of(subscription);
        when(subscriptionRepositoryPort.findSubscriptionsToRenew(any(LocalDate.class), anyInt())).thenReturn(toRenew);
        doThrow(new RuntimeException("Payment failed")).when(paymentPort).debitAmount(any(), any(), any(), any());
        when(subscriptionRepositoryPort.save(any())).thenReturn(subscription);

        List<Subscription> renewed = useCase.execute();

        assertThat(renewed).isEmpty();
        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getRenewalAttempts()).isEqualTo(1);
        assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void execute_shouldSuspendSubscriptionAfterMaxAttempts() {
        subscription.setRenewalAttempts(2);
        List<Subscription> toRenew = List.of(subscription);
        when(subscriptionRepositoryPort.findSubscriptionsToRenew(any(LocalDate.class), anyInt())).thenReturn(toRenew);
        doThrow(new RuntimeException("Payment failed")).when(paymentPort).debitAmount(any(), any(), any(), any());
        when(subscriptionRepositoryPort.save(any())).thenReturn(subscription);

        useCase.execute();

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().getRenewalAttempts()).isEqualTo(3);
        assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
    }

    @Test
    void execute_shouldHandleMultipleSubscriptionsWithMixedResults() {
        Subscription sub2 = new Subscription();
        sub2.setId(UUID.randomUUID());
        sub2.setUser(user);
        sub2.setPlan(TypePlan.PREMIUM);
        sub2.setStatus(SubscriptionStatus.ACTIVE);
        sub2.setRenewalAttempts(2);

        List<Subscription> toRenew = List.of(subscription, sub2);
        when(subscriptionRepositoryPort.findSubscriptionsToRenew(any(LocalDate.class), anyInt())).thenReturn(toRenew);
        when(subscriptionRepositoryPort.save(any())).thenReturn(subscription, sub2);
        doNothing().when(paymentPort).debitAmount(
                eq(user.getId()), 
                eq(TypePlan.BASIC.getPrice()), 
                eq("Renovação de " + TypePlan.BASIC.getDescription()), 
                eq(subscription.getId())
        );
        doThrow(new RuntimeException("Payment failed")).when(paymentPort).debitAmount(
                eq(user.getId()), 
                eq(TypePlan.PREMIUM.getPrice()), 
                eq("Renovação de " + TypePlan.PREMIUM.getDescription()), 
                eq(sub2.getId())
        );

        List<Subscription> renewed = useCase.execute();

        assertThat(renewed).containsExactly(subscription);
        verify(paymentPort).debitAmount(
                eq(user.getId()), 
                eq(TypePlan.BASIC.getPrice()), 
                eq("Renovação de " + TypePlan.BASIC.getDescription()), 
                eq(subscription.getId())
        );
        verify(paymentPort).debitAmount(
                eq(user.getId()), 
                eq(TypePlan.PREMIUM.getPrice()), 
                eq("Renovação de " + TypePlan.PREMIUM.getDescription()), 
                eq(sub2.getId())
        );
        verify(subscriptionRepositoryPort, times(2)).save(any());
    }

    @Test
    void execute_shouldReturnEmptyListWhenNoSubscriptionsToRenew() {
        when(subscriptionRepositoryPort.findSubscriptionsToRenew(any(LocalDate.class), anyInt())).thenReturn(new ArrayList<>());
        List<Subscription> renewed = useCase.execute();
        assertThat(renewed).isEmpty();
        verify(paymentPort, never()).debitSubscriptionPlan(any(), any(), any());
        verify(subscriptionRepositoryPort, never()).save(any());
    }
}
