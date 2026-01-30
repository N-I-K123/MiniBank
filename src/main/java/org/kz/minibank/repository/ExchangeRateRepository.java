package org.kz.minibank.repository;

import org.kz.minibank.model.CurrencyPair;
import org.kz.minibank.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    ExchangeRate findTopByPairOrderByTimestampDesc(CurrencyPair pair);
}
