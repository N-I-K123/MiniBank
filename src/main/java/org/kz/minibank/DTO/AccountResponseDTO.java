package org.kz.minibank.DTO;

import java.math.BigDecimal;

public record AccountResponseDTO(
        Long id,
        String accountNumber,
        BigDecimal balance,
        String currencyCode,
        String ownerName,
        String ownerSurname,
        String ownerEmail
) {
}
