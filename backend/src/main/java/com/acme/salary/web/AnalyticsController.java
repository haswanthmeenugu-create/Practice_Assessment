package com.acme.salary.web;

import com.acme.salary.service.AnalyticsService;
import com.acme.salary.web.dto.analytics.DistributionResponse;
import com.acme.salary.web.dto.analytics.GroupPayResponse;
import com.acme.salary.web.dto.analytics.OverviewResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only pay analytics endpoints.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public OverviewResponse overview() {
        return service.overview();
    }

    /** dimension is one of country, department, level (case-insensitive). */
    @GetMapping("/by/{dimension}")
    public GroupPayResponse byDimension(@PathVariable String dimension) {
        AnalyticsService.Dimension dim =
                AnalyticsService.Dimension.valueOf(dimension.trim().toUpperCase());
        return service.byDimension(dim);
    }

    @GetMapping("/distribution")
    public DistributionResponse distribution() {
        return service.distribution();
    }
}
