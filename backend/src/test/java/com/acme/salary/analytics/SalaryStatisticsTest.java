package com.acme.salary.analytics;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryStatisticsTest {

    private static List<BigDecimal> money(String... values) {
        return List.of(values).stream().map(BigDecimal::new).toList();
    }

    @Test
    void medianOfEmptyListIsNull() {
        assertThat(SalaryStatistics.median(List.of())).isNull();
    }

    @Test
    void medianOfOddCountIsMiddleValue() {
        BigDecimal median = SalaryStatistics.median(money("100", "200", "300"));
        assertThat(median).isEqualByComparingTo("200");
    }

    @Test
    void medianOfEvenCountIsAverageOfMiddleTwo() {
        BigDecimal median = SalaryStatistics.median(money("100", "200", "300", "500"));
        assertThat(median).isEqualByComparingTo("250.00");
    }

    @Test
    void histogramOfEmptyListIsEmpty() {
        assertThat(SalaryStatistics.histogram(List.of(), 5)).isEmpty();
    }

    @Test
    void histogramWithAllEqualValuesIsSingleBucket() {
        List<SalaryStatistics.Bucket> buckets =
                SalaryStatistics.histogram(money("100", "100", "100"), 4);
        assertThat(buckets).hasSize(1);
        assertThat(buckets.get(0).count()).isEqualTo(3);
        assertThat(buckets.get(0).lowerInclusive()).isEqualByComparingTo("100");
        assertThat(buckets.get(0).upperExclusive()).isEqualByComparingTo("100");
    }

    @Test
    void histogramAssignsEveryValueAndPreservesTotalCount() {
        List<BigDecimal> values = money("10", "20", "30", "40", "50", "60", "70", "80", "90", "100");
        List<SalaryStatistics.Bucket> buckets = SalaryStatistics.histogram(values, 5);

        assertThat(buckets).hasSize(5);
        long total = buckets.stream().mapToLong(SalaryStatistics.Bucket::count).sum();
        assertThat(total).isEqualTo(values.size());

        // Buckets must be contiguous and ascending.
        for (int i = 1; i < buckets.size(); i++) {
            assertThat(buckets.get(i).lowerInclusive())
                    .isEqualByComparingTo(buckets.get(i - 1).upperExclusive());
        }
    }

    @Test
    void histogramPutsMaximumValueInLastBucket() {
        List<BigDecimal> values = money("0", "100");
        List<SalaryStatistics.Bucket> buckets = SalaryStatistics.histogram(values, 2);
        assertThat(buckets.get(buckets.size() - 1).count()).isGreaterThanOrEqualTo(1);
        long total = buckets.stream().mapToLong(SalaryStatistics.Bucket::count).sum();
        assertThat(total).isEqualTo(2);
    }
}
