package org.kz.minibank.service;


import org.kz.minibank.clinet.ForexClient;
import org.kz.minibank.model.CurrencyPair;
import org.kz.minibank.model.ExchangeRate;
import org.kz.minibank.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class ExchangeRateService {
    private final ForexClient forexClient;
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ForexClient forexClient, ExchangeRateRepository exchangeRateRepository) {
        this.forexClient = forexClient;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public BigDecimal convert (BigDecimal amount, CurrencyPair pair) {
        ExchangeRate exchangeRate = exchangeRateRepository.findTopByPairOrderByTimestampDesc(pair);
        CurrencyPair reversePair = new CurrencyPair(pair.counter(), pair.base());
        if (exchangeRate == null) {
            exchangeRate = exchangeRateRepository.findTopByPairOrderByTimestampDesc(reversePair);
            if (exchangeRate != null && !isRateStale(exchangeRate)) return amount.divide(exchangeRate.getRate(), 2, RoundingMode.HALF_UP);
        }


        if (exchangeRate == null || isRateStale(exchangeRate)) {
            BigDecimal fromRate = forexClient.getExchangeRate(pair.base());
            BigDecimal toRate = forexClient.getExchangeRate(pair.counter());

            BigDecimal crossRate = fromRate.divide(toRate, 4, RoundingMode.HALF_UP);

            ExchangeRate newRate = new ExchangeRate();
            if (exchangeRate!= null && exchangeRate.getPair().equals(reversePair)){
                BigDecimal reversedCrossRate = toRate.divide(fromRate, 4, RoundingMode.HALF_UP);
                newRate.setPair(reversePair);
                newRate.setRate(reversedCrossRate);
                exchangeRateRepository.save(newRate);
                return amount.divide(reversedCrossRate, 2, RoundingMode.HALF_UP);
            } else {
                newRate.setPair(pair);
                newRate.setRate(crossRate);
                exchangeRateRepository.save(newRate);
                return amount.multiply(crossRate).setScale(2, RoundingMode.HALF_UP);
            }
        }
        return amount.multiply(exchangeRate.getRate()).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isRateStale(ExchangeRate rate) {
        return rate.getTimestamp().isBefore(LocalDateTime.now().minusMinutes(5));
    }
}
