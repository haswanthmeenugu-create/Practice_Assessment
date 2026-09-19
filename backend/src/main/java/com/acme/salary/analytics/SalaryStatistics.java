package com.acme.salary.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure statistical helpers over a list of salary values. Deliberately free of
 * Spring, JPA, and database concerns so the math is trivially unit-testable and
 * portable across databases (percentile/median SQL differs between MySQL and
 * H2, so we compute it here instead).
 */
public final class SalaryStatistics {

    private SalaryStatistics() {
    }

    /**
     * Median of the given values.
     *
     * @param sortedAscending values sorted ascending; must not be null
     * @return the median, or {@code null} if the list is empty
     */
    public static BigDecimal median(List<BigDecimal> sortedAscending) {
        int n = sortedAscending.size();
        if (n == 0) {
            return null;
        }
        if (n % 2 == 1) {
            return sortedAscending.get(n / 2);
        }
        BigDecimal lower = sortedAscending.get(n / 2 - 1);
        BigDecimal upper = sortedAscending.get(n / 2);
        return lower.add(upper).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    /**
     * Fixed-width histogram of {@code values} into {@code bucketCount} buckets
     * spanning [min, max]. Each bucket is half-open [lowerInclusive,
     * upperExclusive) except the last, which includes the maximum.
     *
     * @return buckets in ascending order; empty list if there are no values
     */
    public static List<Bucket> histogram(List<BigDecimal> values, int bucketCount) {
        if (bucketCount < 1) {
            throw new IllegalArgumentException("bucketCount must be >= 1");
        }
        List<Bucket> buckets = new ArrayList<>();
        if (values.isEmpty()) {
            return buckets;
        }

        BigDecimal min = values.get(0);
        BigDecimal max = values.get(0);
        for (BigDecimal v : values) {
            if (v.compareTo(min) < 0) {
                min = v;
            }
            if (v.compareTo(max) > 0) {
                max = v;
            }
        }

        // Degenerate case: all values equal -> single bucket holding everything.
        if (min.compareTo(max) == 0) {
            buckets.add(new Bucket(min, max, values.size()));
            return buckets;
        }

        BigDecimal range = max.subtract(min);
        BigDecimal width = range.divide(BigDecimal.valueOf(bucketCount), 2, RoundingMode.UP);

        long[] counts = new long[bucketCount];
        for (BigDecimal v : values) {
            int idx = v.subtract(min).divide(width, 0, RoundingMode.FLOOR).intValue();
            if (idx < 0) {
                idx = 0;
            }
            if (idx >= bucketCount) {
                idx = bucketCount - 1; // max value falls into the last bucket
            }
            counts[idx]++;
        }

        BigDecimal lower = min;
        for (int i = 0; i < bucketCount; i++) {
            BigDecimal upper = (i == bucketCount - 1) ? max : lower.add(width);
            buckets.add(new Bucket(lower, upper, counts[i]));
            lower = upper;
        }
        return buckets;
    }

    /** A single histogram bucket: [lowerInclusive, upperExclusive) and its count. */
    public record Bucket(BigDecimal lowerInclusive, BigDecimal upperExclusive, long count) {
    }
}
