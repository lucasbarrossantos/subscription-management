package com.globo.subscription.adapter.integration.wallet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

import com.globo.subscription.adapter.integration.wallet.dto.WalletResponse;

@FeignClient(name = "walletClient", url = "${integrations.wallet.url}")
public interface WalletFeignClient {

    @GetMapping("/wallets/{userId}")
    WalletResponse getWallet(@PathVariable("userId") UUID userId);
}