package com.acme.salary.service;

import com.acme.salary.domain.Currency;
import com.acme.salary.domain.Department;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.EmploymentType;
import com.acme.salary.domain.Level;
import com.acme.salary.money.StaticRateCurrencyConverter;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.web.dto.analytics.DistributionResponse;
import com.acme.salary.web.dto.analytics.GroupPayResponse;
import com.acme.salary.web.dto.analytics.OverviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AnalyticsService.class, StaticRateCurrencyConverter.class})
class AnalyticsServiceTest {

    @Autowired
    private EmployeeRepository repository;

    @Autowired
    private AnalyticsService analytics;

    private int counter = 0;

    @BeforeEach
    void seed() {
        // Active employees, all in USD for easy expected math:
        // 60k, 80k, 100k, 120k, 140k  -> avg 100k, median 100k, total 500k
        addUsdActive("US", Department.ENGINEERING, Level.JUNIOR, "60000");
        addUsdActive("US", Department.ENGINEERING, Level.MID, "80000");
        addUsdActive("GB", Department.SALES, Level.SENIOR, "100000");
        addUsdActive("GB", Department.SALES, Level.SENIOR, "120000");
        addUsdActive("IN", Department.DATA, Level.STAFF, "140000");
        // A terminated employee must be excluded from analytics.
        Employee terminated = base("US", Department.ENGINEERING, Level.VP, "999000");
        terminated.setStatus(EmployeeStatus.TERMINATED);
        repository.save(terminated);
    }

    @Test
    void overviewComputesOverActiveOnly() {
        OverviewResponse overview = analytics.overview();
        assertThat(overview.baseCurrency()).isEqualTo("USD");
        assertThat(overview.activeHeadcount()).isEqualTo(5);
        assertThat(overview.totalPayroll()).isEqualByComparingTo("500000.00");
        assertThat(overview.averageSalary()).isEqualByComparingTo("100000.00");
        assertThat(overview.medianSalary()).isEqualByComparingTo("100000.00");
        assertThat(overview.minSalary()).isEqualByComparingTo("60000.00");
        assertThat(overview.maxSalary()).isEqualByComparingTo("140000.00");
    }

    @Test
    void byCountryGroupsAndAggregates() {
        GroupPayResponse byCountry = analytics.byDimension(AnalyticsService.Dimension.COUNTRY);
        assertThat(byCountry.dimension()).isEqualTo("COUNTRY");

        GroupPayResponse.Group gb = byCountry.groups().stream()
                .filter(g -> g.key().equals("GB")).findFirst().orElseThrow();
        assertThat(gb.headcount()).isEqualTo(2);
        assertThat(gb.averagePay()).isEqualByComparingTo("110000.00");
        assertThat(gb.totalPay()).isEqualByComparingTo("220000.00");
    }

    @Test
    void distributionCoversAllActiveEmployees() {
        DistributionResponse distribution = analytics.distribution();
        long total = distribution.buckets().stream()
                .mapToLong(DistributionResponse.Bucket::count).sum();
        assertThat(total).isEqualTo(5);
    }

    private void addUsdActive(String country, Department dept, Level level, String salary) {
        repository.save(base(country, dept, level, salary));
    }

    private Employee base(String country, Department dept, Level level, String salary) {
        counter++;
        Employee e = new Employee();
        e.setEmployeeCode("ACME-T" + counter);
        e.setFirstName("Test");
        e.setLastName("User" + counter);
        e.setEmail("user" + counter + "@acme.example");
        e.setCountry(country);
        e.setDepartment(dept);
        e.setJobTitle("Tester");
        e.setLevel(level);
        e.setBaseSalary(new BigDecimal(salary));
        e.setCurrency(Currency.USD);
        e.setBaseSalaryUsd(new BigDecimal(salary));
        e.setEmploymentType(EmploymentType.FULL_TIME);
        e.setStatus(EmployeeStatus.ACTIVE);
        e.setHireDate(LocalDate.of(2022, 1, 1));
        return e;
    }
}
