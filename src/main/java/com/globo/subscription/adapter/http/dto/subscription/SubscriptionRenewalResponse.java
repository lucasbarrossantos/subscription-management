package com.globo.subscription.adapter.http.dto.subscription;

import java.util.List;

public record SubscriptionRenewalResponse(
    int totalProcessed,
    int successCount,
    List<SubscriptionResponse> renewedSubscriptions
) {}
