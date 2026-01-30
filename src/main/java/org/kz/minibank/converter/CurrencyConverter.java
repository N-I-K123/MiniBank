package org.kz.minibank.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Currency;

@Converter(autoApply = true)
public class CurrencyConverter implements AttributeConverter <Currency, String> {

    @Override
    public String convertToDatabaseColumn(Currency currency) {
        if (currency == null) return null;
        return currency.getCurrencyCode();
    }

    @Override
    public Currency convertToEntityAttribute(String currencyCode) {
        if (currencyCode == null || currencyCode.isEmpty()) return null;
        try {
            return Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) { return null; }
    }
}
