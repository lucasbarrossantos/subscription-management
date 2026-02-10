package com.globo.subscription.presentation.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserRequest (

    @NotEmpty(message = "{user.name.required}")
    String name,
    @NotEmpty(message = "{user.email.required}")
    @Email(message = "{user.email.invalid}")
    String email
) {}