package com.globo.subscription.adapter.integration.wallet;

import com.globo.subscription.adapter.integration.wallet.dto.WalletResponse;
import com.globo.subscription.core.exception.BusinessException;
import com.globo.subscription.core.port.out.wallet.WalletPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletAdapter implements WalletPort {

    private final WalletFeignClient walletFeignClient;

    @Override
    public boolean existsWallet(UUID userId) {

        try {
            WalletResponse response = walletFeignClient.getWallet(userId);
            return response != null && response.userId() != null;
        } catch (HttpClientErrorException.NotFound e) {
            log.info("Carteira não encontrada para usuário {}", userId);
            return false;
        } catch (Exception exception) {
            log.error("Erro ao consultar carteira do usuário {}", userId, exception);
            throw new BusinessException("Erro ao consultar carteira do usuário " + userId);
        }
    }
}
