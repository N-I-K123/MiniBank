package org.kz.minibank.converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.kz.minibank.model.CurrencyPair;

import java.util.Currency;

@Converter(autoApply = true)
public class CurrencyPairConverter implements AttributeConverter<CurrencyPair, String> {

    @Override
    public String convertToDatabaseColumn(CurrencyPair pair){
        if (pair == null) return null;

        return pair.base().getCurrencyCode() + "-" + pair.counter().getCurrencyCode();
    }

    @Override
    public CurrencyPair convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || dbData.contains("/")) return null;

        String[] pair = dbData.split("-");

        try {
            Currency base = Currency.getInstance(pair[0]);
            Currency counter = Currency.getInstance(pair[1]);

            return new CurrencyPair(base, counter);
        }catch (IllegalArgumentException e) { return null; }
    }
}
