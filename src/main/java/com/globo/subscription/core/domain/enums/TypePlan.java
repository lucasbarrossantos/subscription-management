package com.globo.subscription.core.domain.enums;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public enum TypePlan {

    BASIC(new BigDecimal("19.90"), "Plano BASICO"),
    PREMIUM(new BigDecimal("39.90"), "Plano PREMIUM"),
    FAMILY(new BigDecimal("59.90"), "Plano FAMILIA");

    private final BigDecimal price;
    private final String description;

    TypePlan(BigDecimal price, String description) {
        this.price = price;
        this.description = description;
    }
}