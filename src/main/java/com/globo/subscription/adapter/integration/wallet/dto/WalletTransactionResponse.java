package com.globo.subscription.adapter.integration.wallet.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionResponse {

    private UUID id;
    private UUID walletId;
    private String type;
    private BigDecimal amount;
    private String description;
    private OffsetDateTime createdAt;
}
