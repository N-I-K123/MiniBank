package org.kz.minibank.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.Currency;

public record CreateAccountRequest(
        @NotNull
        String currencyCode
) {
}
