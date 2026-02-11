package com.globo.subscription.adapter.http.dto.user;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email
) {}