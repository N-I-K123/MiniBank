package org.kz.minibank.DTO;

import org.kz.minibank.model.TransactionStatus;

import java.math.BigDecimal;

public record TransactionResponseDTO(
        BigDecimal amount,
        String currencyCode,
        String sourceAccountNumber,
        String targetAccountNumber,
        String title,
        TransactionStatus status
) {
}
