package com.globo.subscription.adapter.http.dto.subscription;

import java.util.UUID;

public record UpdateSubscriptionStatusRequest(UUID subscriptionId, String status) {}
