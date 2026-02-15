package com.globo.subscription.adapter.integration.wallet;

import com.globo.subscription.adapter.integration.wallet.dto.WalletResponse;
import com.globo.subscription.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class WalletAdapterTest {

    @Mock
    private WalletFeignClient walletFeignClient;
    @InjectMocks
    private WalletAdapter adapter;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
    }

    @Test
    void existsWallet_shouldReturnTrueWhenWalletExists() {
        WalletResponse response = mock(WalletResponse.class);
        when(response.userId()).thenReturn(userId);
        when(walletFeignClient.getWallet(userId)).thenReturn(response);
        boolean result = adapter.existsWallet(userId);
        assertThat(result).isTrue();
    }

    @Test
    void existsWallet_shouldReturnFalseWhenWalletNotFound() {
        when(walletFeignClient.getWallet(userId)).thenThrow(HttpClientErrorException.NotFound.class);
        boolean result = adapter.existsWallet(userId);
        assertThat(result).isFalse();
    }

    @Test
    void existsWallet_shouldThrowBusinessExceptionOnOtherErrors() {
        when(walletFeignClient.getWallet(userId)).thenThrow(new RuntimeException("Unexpected error"));
        assertThatThrownBy(() -> adapter.existsWallet(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Erro ao consultar carteira do usuário");
    }

    @Test
    void existsWallet_shouldReturnFalseWhenResponseIsNull() {
        when(walletFeignClient.getWallet(userId)).thenReturn(null);
        boolean result = adapter.existsWallet(userId);
        assertThat(result).isFalse();
    }

    @Test
    void existsWallet_shouldReturnFalseWhenUserIdIsNullInResponse() {
        WalletResponse response = mock(WalletResponse.class);
        when(response.userId()).thenReturn(null);
        when(walletFeignClient.getWallet(userId)).thenReturn(response);
        boolean result = adapter.existsWallet(userId);
        assertThat(result).isFalse();
    }
}
