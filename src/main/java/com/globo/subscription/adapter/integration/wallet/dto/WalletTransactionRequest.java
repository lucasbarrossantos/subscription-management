package com.globo.subscription.adapter.integration.wallet.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionRequest {

    private TransactionType type;
    private BigDecimal amount;
    private String description;
}
