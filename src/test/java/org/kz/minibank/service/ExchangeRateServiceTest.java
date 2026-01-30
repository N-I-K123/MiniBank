package org.kz.minibank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kz.minibank.clinet.ForexClient;
import org.kz.minibank.model.CurrencyPair;
import org.kz.minibank.model.ExchangeRate;
import org.kz.minibank.repository.ExchangeRateRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private ForexClient forexClient;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    private CurrencyPair pair;
    private CurrencyPair reversePair;
    private ExchangeRate exchangeRate;

    @BeforeEach
    void setUp() {
        pair = new CurrencyPair(Currency.getInstance("USD"), Currency.getInstance("PLN"));
        reversePair = new CurrencyPair(Currency.getInstance("PLN"), Currency.getInstance("USD"));

        exchangeRate = new ExchangeRate();
        exchangeRate.setPair(pair);
        exchangeRate.setRate(new BigDecimal("4.0000"));
        exchangeRate.setTimestamp(LocalDateTime.now());
    }

    @Test
    void convert_ExistingValidRate_Success() {
        when(exchangeRateRepository.findTopByPairOrderByTimestampDesc(pair)).thenReturn(exchangeRate);

        BigDecimal result = exchangeRateService.convert(new BigDecimal("100"), pair);

        assertEquals(0, new BigDecimal("400.00").compareTo(result), "Values should be mathematically equal");
        verify(forexClient, never()).getExchangeRate(any());
    }

    @Test
    void convert_ExistingValidReverseRate_Success() {
        when(exchangeRateRepository.findTopByPairOrderByTimestampDesc(pair)).thenReturn(null);

        ExchangeRate reverseRate = new ExchangeRate();
        reverseRate.setPair(reversePair);
        reverseRate.setRate(new BigDecimal("0.3300"));
        reverseRate.setTimestamp(LocalDateTime.now());

        when(exchangeRateRepository.findTopByPairOrderByTimestampDesc(reversePair)).thenReturn(reverseRate);

        BigDecimal result = exchangeRateService.convert(new BigDecimal("100"), pair);

        BigDecimal expected = new BigDecimal("303.03");

        assertEquals(0, expected.compareTo(result),
                "Should calculate reverse rate for periodic fractions (100 / 0.33)");

        verify(forexClient, never()).getExchangeRate(any());
    }

    @Test
    void convert_RateStale_FetchesFromForex() {
        exchangeRate.setTimestamp(LocalDateTime.now().minusMinutes(10)); // Stale
        when(exchangeRateRepository.findTopByPairOrderByTimestampDesc(pair)).thenReturn(exchangeRate);

        when(forexClient.getExchangeRate(Currency.getInstance("USD"))).thenReturn(new BigDecimal("4.0"));
        when(forexClient.getExchangeRate(Currency.getInstance("PLN"))).thenReturn(new BigDecimal("1.0"));

        BigDecimal result = exchangeRateService.convert(new BigDecimal("100"), pair);

        assertEquals(0, new BigDecimal("400.00").compareTo(result), "Values should be mathematically equal");
        verify(exchangeRateRepository).save(any(ExchangeRate.class));
    }

    @Test
    void convert_NoRate_FetchesFromForex() {
        when(exchangeRateRepository.findTopByPairOrderByTimestampDesc(pair)).thenReturn(null);
        when(exchangeRateRepository.findTopByPairOrderByTimestampDesc(reversePair)).thenReturn(null);

        when(forexClient.getExchangeRate(Currency.getInstance("USD"))).thenReturn(new BigDecimal("4.0"));
        when(forexClient.getExchangeRate(Currency.getInstance("PLN"))).thenReturn(new BigDecimal("1.0"));

        BigDecimal result = exchangeRateService.convert(new BigDecimal("100"), pair);

        assertEquals(0, new BigDecimal("400.00").compareTo(result), "Values should be mathematically equal");
        verify(exchangeRateRepository).save(any(ExchangeRate.class));
    }
}
