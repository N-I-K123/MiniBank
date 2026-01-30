package org.kz.minibank.DTO;

import java.math.BigDecimal;

public record NbpRate(
        String no,
        String effectiveData,
        BigDecimal mid
){}
