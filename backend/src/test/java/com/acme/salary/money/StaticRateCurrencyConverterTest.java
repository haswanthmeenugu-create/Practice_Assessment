package com.acme.salary.money;

import com.acme.salary.domain.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class StaticRateCurrencyConverterTest {

    private final StaticRateCurrencyConverter converter = new StaticRateCurrencyConverter();

    @Test
    void baseCurrencyIsUsd() {
        assertThat(converter.baseCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    void usdConvertsToItselfUnchanged() {
        assertThat(converter.toBase(new BigDecimal("1234.56"), Currency.USD))
                .isEqualByComparingTo("1234.56");
    }

    @Test
    void nonUsdIsDividedByRateToGetUsd() {
        // 8300 INR at 83 INR/USD = 100 USD
        assertThat(converter.toBase(new BigDecimal("8300.00"), Currency.INR))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void fromBaseIsInverseOfToBase() {
        BigDecimal usd = new BigDecimal("100.00");
        BigDecimal local = converter.fromBase(usd, Currency.GBP);
        assertThat(converter.toBase(local, Currency.GBP)).isEqualByComparingTo(usd);
    }

    @Test
    void fromBaseMultipliesByRate() {
        // 100 USD at 0.92 EUR/USD = 92 EUR
        assertThat(converter.fromBase(new BigDecimal("100.00"), Currency.EUR))
                .isEqualByComparingTo("92.00");
    }
}
