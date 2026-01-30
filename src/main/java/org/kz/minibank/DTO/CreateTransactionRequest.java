package org.kz.minibank.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull
        String sourceAccountNumber,
        @NotNull
        String targetAccountNumber,

        @NotNull
        @DecimalMin(value = "0.01", message = "amount must be grater than 0!")
        @Digits(integer = 10, fraction = 2, message = "amount must be valid decimal number!")
        BigDecimal amount,

        @NotNull
        String title
) {
}
