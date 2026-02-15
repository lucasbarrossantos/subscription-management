package com.globo.subscription.core.port.out.wallet;

import java.util.UUID;

public interface WalletPort {
    boolean existsWallet(UUID userId);
}
