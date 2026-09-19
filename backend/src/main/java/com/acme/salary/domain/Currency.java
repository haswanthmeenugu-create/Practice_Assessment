package com.acme.salary.domain;

/**
 * Currencies employees can be paid in. The organization spans multiple
 * countries, so a salary is only meaningful alongside its currency.
 *
 * <p>FX normalization for analytics lives in
 * {@code com.acme.salary.money.CurrencyConverter}, not here — the enum stays a
 * plain identifier so the rate source can be swapped without touching the model.
 */
public enum Currency {
    USD,
    EUR,
    GBP,
    INR,
    CAD,
    AUD,
    SGD,
    JPY,
    BRL,
    ZAR
}
