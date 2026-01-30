package org.kz.minibank.DTO;

import java.util.List;

public record NbpResponse(
        String code,
        List<NbpRate> rates
) {}

