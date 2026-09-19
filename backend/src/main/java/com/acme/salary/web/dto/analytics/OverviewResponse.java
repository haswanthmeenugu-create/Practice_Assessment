package com.acme.salary.web.dto.analytics;

import java.math.BigDecimal;

/**
 * Org-wide pay snapshot for active employees, all monetary values in the base
 * currency (USD).
 */
public record OverviewResponse(
        String baseCurrency,
        long activeHeadcount,
        BigDecimal totalPayroll,
        BigDecimal averageSalary,
        BigDecimal medianSalary,
        BigDecimal minSalary,
        BigDecimal maxSalary
) {
}
