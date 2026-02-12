package com.globo.subscription.adapter.integration.wallet;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.globo.subscription.adapter.integration.wallet.dto.TransactionType;
import com.globo.subscription.adapter.integration.wallet.dto.WalletTransactionRequest;
import com.globo.subscription.adapter.integration.wallet.dto.WalletTransactionResponse;
import com.globo.subscription.core.domain.enums.TypePlan;
import com.globo.subscription.core.exception.WalletTransactionException;
import com.globo.subscription.core.port.out.wallet.WalletPort;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletAdapter implements WalletPort {

    private final WalletFeignClient walletFeignClient;

    @Override
    public void debitSubscriptionPlan(UUID userId, TypePlan plan) {
        debitAmount(userId, plan.getPrice(), "Compra de " + plan.getDescription());
    }

    @Override
    public void debitAmount(UUID userId, BigDecimal amount, String description) {
        try {
            log.info("Debiting wallet for user {} - amount: {}, description: {}", userId, amount, description);

            WalletTransactionRequest request = WalletTransactionRequest.builder()
                    .type(TransactionType.DEBIT)
                    .amount(amount)
                    .description(description)
                    .build();

            WalletTransactionResponse response = walletFeignClient.createTransaction(userId, request);

            log.info("Wallet debited successfully for user {} - transaction id: {}", userId, response.getId());
        } catch (FeignException.UnprocessableEntity e) {
            log.error("Insufficient balance for user {} - amount: {}", userId, amount, e);
            throw new WalletTransactionException("Saldo insuficiente na carteira para realizar a transação de R$ " + amount);
        } catch (FeignException.NotFound e) {
            log.error("Wallet not found for user {}", userId, e);
            throw new WalletTransactionException("Carteira não encontrada para o usuário");
        } catch (FeignException e) {
            log.error("Error debiting wallet for user {} - status: {}", userId, e.status(), e);
            throw new WalletTransactionException("Erro ao processar débito na carteira: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error debiting wallet for user {}", userId, e);
            throw new WalletTransactionException("Erro inesperado ao processar débito na carteira", e);
        }
    }

    @Override
    public void creditRefund(UUID userId, BigDecimal amount, String description) {
        try {
            log.info("Crediting wallet (refund) for user {} - amount: {}", userId, amount);

            WalletTransactionRequest request = WalletTransactionRequest.builder()
                    .type(TransactionType.CREDIT)
                    .amount(amount)
                    .description(description)
                    .build();

            WalletTransactionResponse response = walletFeignClient.createTransaction(userId, request);

            log.info("Wallet credited successfully for user {} - transaction id: {}", userId, response.getId());
        } catch (FeignException.NotFound e) {
            log.error("Wallet not found for user {}", userId, e);
            throw new WalletTransactionException("Carteira não encontrada para o usuário");
        } catch (FeignException e) {
            log.error("Error crediting wallet for user {} - status: {}", userId, e.status(), e);
            throw new WalletTransactionException("Erro ao processar crédito na carteira: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error crediting wallet for user {}", userId, e);
            throw new WalletTransactionException("Erro inesperado ao processar crédito na carteira", e);
        }
    }
}
