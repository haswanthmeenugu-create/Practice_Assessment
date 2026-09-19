package com.acme.salary.service;

import com.acme.salary.analytics.SalaryStatistics;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.money.CurrencyConverter;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.projection.GroupAggregateRow;
import com.acme.salary.web.dto.analytics.DistributionResponse;
import com.acme.salary.web.dto.analytics.GroupPayResponse;
import com.acme.salary.web.dto.analytics.OverviewResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Read-only pay analytics answering "how does the org pay people". All figures
 * are normalized to the base currency (USD). Grouped sums/averages come from
 * DB-side aggregation; median and distribution are computed from a single
 * ordered projection of active salaries (portable across MySQL and H2).
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    /** Grouping dimensions the "by" endpoint understands. */
    public enum Dimension {
        COUNTRY,
        DEPARTMENT,
        LEVEL
    }

    private static final int DEFAULT_BUCKETS = 10;

    private final EmployeeRepository repository;
    private final String baseCurrency;

    public AnalyticsService(EmployeeRepository repository, CurrencyConverter currencyConverter) {
        this.repository = repository;
        this.baseCurrency = currencyConverter.baseCurrency().name();
    }

    public OverviewResponse overview() {
        List<BigDecimal> salaries = repository.findActiveSalariesUsdAscending();
        long headcount = salaries.size();
        if (headcount == 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return new OverviewResponse(baseCurrency, 0, zero, zero, zero, zero, zero);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal s : salaries) {
            total = total.add(s);
        }
        BigDecimal average = total.divide(BigDecimal.valueOf(headcount), 2, RoundingMode.HALF_UP);
        BigDecimal median = SalaryStatistics.median(salaries);
        BigDecimal min = salaries.get(0);
        BigDecimal max = salaries.get(salaries.size() - 1);

        return new OverviewResponse(baseCurrency, headcount,
                total.setScale(2, RoundingMode.HALF_UP), average, median, min, max);
    }

    public GroupPayResponse byDimension(Dimension dimension) {
        List<GroupAggregateRow> rows = switch (dimension) {
            case COUNTRY -> repository.aggregateByCountry();
            case DEPARTMENT -> repository.aggregateByDepartment();
            case LEVEL -> repository.aggregateByLevel();
        };
        List<GroupPayResponse.Group> groups = rows.stream()
                .map(r -> new GroupPayResponse.Group(
                        r.getGroupKey(),
                        r.getHeadcount(),
                        scale(r.getTotalUsd()),
                        scale(r.getAvgUsd()),
                        scale(r.getMinUsd()),
                        scale(r.getMaxUsd())))
                .toList();
        return new GroupPayResponse(dimension.name(), baseCurrency, groups);
    }

    public DistributionResponse distribution() {
        List<BigDecimal> salaries = repository.findActiveSalariesUsdAscending();
        List<SalaryStatistics.Bucket> buckets = SalaryStatistics.histogram(salaries, DEFAULT_BUCKETS);
        List<DistributionResponse.Bucket> mapped = buckets.stream()
                .map(b -> new DistributionResponse.Bucket(
                        b.lowerInclusive(), b.upperExclusive(), b.count()))
                .toList();
        return new DistributionResponse(baseCurrency, mapped);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
