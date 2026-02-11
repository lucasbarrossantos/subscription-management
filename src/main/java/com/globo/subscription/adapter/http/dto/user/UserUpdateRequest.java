package com.globo.subscription.adapter.http.dto.user;

import jakarta.validation.constraints.Email;

public record UserUpdateRequest(
    String name,
    @Email(message = "{user.email.invalid}")
    String email
) {}
