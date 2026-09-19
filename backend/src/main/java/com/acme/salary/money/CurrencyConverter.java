package com.acme.salary.money;

import com.acme.salary.domain.Currency;

import java.math.BigDecimal;

/**
 * Converts monetary amounts into a common base currency so salaries paid in
 * different currencies can be compared and aggregated.
 *
 * <p>Kept as an interface so the rate source (static table today, a live feed
 * or cached provider later) can change without touching analytics logic.
 */
public interface CurrencyConverter {

    /** The currency all analytics figures are normalized to. */
    Currency baseCurrency();

    /**
     * Convert {@code amount} expressed in {@code from} into {@link #baseCurrency()}.
     *
     * @return the amount in base currency, never null
     */
    BigDecimal toBase(BigDecimal amount, Currency from);

    /**
     * Convert {@code baseAmount} (in {@link #baseCurrency()}) into {@code to}.
     * Inverse of {@link #toBase}; useful for producing realistic local-currency
     * figures from a base-currency target.
     *
     * @return the amount in {@code to}, never null
     */
    BigDecimal fromBase(BigDecimal baseAmount, Currency to);
}
