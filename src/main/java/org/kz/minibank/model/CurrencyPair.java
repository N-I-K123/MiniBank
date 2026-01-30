package org.kz.minibank.model;

import java.util.Currency;

public record CurrencyPair(Currency base, Currency counter) {
}
