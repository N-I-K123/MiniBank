package org.kz.minibank.clinet;

import org.kz.minibank.DTO.NbpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class ForexClient {
    private final RestClient restClient;

    private static final String NBP_API_URL = "http://api.nbp.pl/api/exchangerates/rates/a/{code}/?format=json";

    public ForexClient() {
        this.restClient = RestClient.create();
    }

    public BigDecimal getExchangeRate(Currency currency){
        if (currency.getCurrencyCode().equals("PLN")) return BigDecimal.ONE;

        NbpResponse response = restClient.get()
                .uri(NBP_API_URL, currency.getCurrencyCode())
                .retrieve()
                .body(NbpResponse.class);

        if (response != null && !response.rates().isEmpty()){
            return response.rates().getFirst().mid();
        }
        throw new IllegalArgumentException("Exchange rate not found!");
    }

}
