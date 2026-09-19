package com.acme.salary.repository.projection;

import java.math.BigDecimal;

/**
 * Spring Data projection for a single grouped aggregate row (one country,
 * department, or level). All monetary values are already normalized to USD.
 */
public interface GroupAggregateRow {

    String getGroupKey();

    long getHeadcount();

    BigDecimal getTotalUsd();

    BigDecimal getAvgUsd();

    BigDecimal getMinUsd();

    BigDecimal getMaxUsd();
}
