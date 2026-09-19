package com.acme.salary.web.dto.analytics;

import java.math.BigDecimal;
import java.util.List;

/**
 * Salary distribution histogram for active employees, in the base currency (USD).
 */
public record DistributionResponse(
        String baseCurrency,
        List<Bucket> buckets
) {
    public record Bucket(
            BigDecimal from,
            BigDecimal to,
            long count
    ) {
    }
}
