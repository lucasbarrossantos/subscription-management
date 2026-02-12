package com.globo.subscription.adapter.integration.wallet;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.globo.subscription.adapter.integration.wallet.dto.WalletTransactionRequest;
import com.globo.subscription.adapter.integration.wallet.dto.WalletTransactionResponse;

@FeignClient(name = "wallet-service", url = "${integrations.wallet.api.url}")
public interface WalletFeignClient {

    @PostMapping("/wallets/{userId}/transactions")
    WalletTransactionResponse createTransaction(
            @PathVariable("userId") UUID userId,
            @RequestBody WalletTransactionRequest request);
}
