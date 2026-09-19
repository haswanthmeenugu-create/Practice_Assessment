package com.acme.salary.web.dto.analytics;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pay broken down by a grouping dimension (country, department, or level).
 * All monetary values are in the base currency (USD).
 */
public record GroupPayResponse(
        String dimension,
        String baseCurrency,
        List<Group> groups
) {
    public record Group(
            String key,
            long headcount,
            BigDecimal totalPay,
            BigDecimal averagePay,
            BigDecimal minPay,
            BigDecimal maxPay
    ) {
    }
}
