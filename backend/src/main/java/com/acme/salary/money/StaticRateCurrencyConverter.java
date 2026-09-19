package com.acme.salary.money;

import com.acme.salary.domain.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * {@link CurrencyConverter} backed by a fixed table of "units of currency per
 * 1 USD". Deliberately static (see docs/REQUIREMENTS.md): a live FX feed would
 * add an external dependency and make results non-deterministic. The rates are
 * versioned in code and good enough to make cross-country pay comparisons
 * meaningful.
 *
 * <p>Example: EUR rate 0.92 means 1 USD = 0.92 EUR, so an amount in EUR is
 * divided by 0.92 to get USD.
 */
@Component
public class StaticRateCurrencyConverter implements CurrencyConverter {

    private static final Currency BASE = Currency.USD;

    /** Units of the given currency per 1 USD. Snapshot; not live. */
    private static final Map<Currency, BigDecimal> RATES_PER_USD = new EnumMap<>(Currency.class);

    static {
        RATES_PER_USD.put(Currency.USD, new BigDecimal("1.00"));
        RATES_PER_USD.put(Currency.EUR, new BigDecimal("0.92"));
        RATES_PER_USD.put(Currency.GBP, new BigDecimal("0.79"));
        RATES_PER_USD.put(Currency.INR, new BigDecimal("83.00"));
        RATES_PER_USD.put(Currency.CAD, new BigDecimal("1.36"));
        RATES_PER_USD.put(Currency.AUD, new BigDecimal("1.52"));
        RATES_PER_USD.put(Currency.SGD, new BigDecimal("1.34"));
        RATES_PER_USD.put(Currency.JPY, new BigDecimal("149.00"));
        RATES_PER_USD.put(Currency.BRL, new BigDecimal("5.05"));
        RATES_PER_USD.put(Currency.ZAR, new BigDecimal("18.60"));
    }

    @Override
    public Currency baseCurrency() {
        return BASE;
    }

    @Override
    public BigDecimal toBase(BigDecimal amount, Currency from) {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(from, "from");
        if (from == BASE) {
            return amount;
        }
        BigDecimal rate = RATES_PER_USD.get(from);
        if (rate == null) {
            throw new IllegalArgumentException("No FX rate configured for " + from);
        }
        // amount is in `from`; divide by (from per USD) to get USD.
        return amount.divide(rate, 2, java.math.RoundingMode.HALF_UP);
    }
}
