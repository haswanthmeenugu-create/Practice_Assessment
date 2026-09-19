package com.acme.salary.repository;

import com.acme.salary.domain.Employee;
import com.acme.salary.repository.projection.GroupAggregateRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Persistence for {@link Employee}. Extends {@link JpaSpecificationExecutor} so
 * list filtering is composed dynamically (see {@code EmployeeSpecifications})
 * rather than via a combinatorial explosion of query methods.
 *
 * <p>Analytics aggregation is expressed as JPQL {@code GROUP BY} on the
 * pre-normalized {@code baseSalaryUsd} column so it runs in the database and
 * stays portable across MySQL (runtime) and H2 (tests).
 */
public interface EmployeeRepository
        extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    /** Per-country aggregates over the given status, normalized to USD. */
    @Query("""
            select e.country as groupKey, count(e) as headcount,
                   sum(e.baseSalaryUsd) as totalUsd, avg(e.baseSalaryUsd) as avgUsd,
                   min(e.baseSalaryUsd) as minUsd, max(e.baseSalaryUsd) as maxUsd
            from Employee e
            where e.status = com.acme.salary.domain.EmployeeStatus.ACTIVE
            group by e.country
            order by e.country
            """)
    List<GroupAggregateRow> aggregateByCountry();

    @Query("""
            select cast(e.department as string) as groupKey, count(e) as headcount,
                   sum(e.baseSalaryUsd) as totalUsd, avg(e.baseSalaryUsd) as avgUsd,
                   min(e.baseSalaryUsd) as minUsd, max(e.baseSalaryUsd) as maxUsd
            from Employee e
            where e.status = com.acme.salary.domain.EmployeeStatus.ACTIVE
            group by e.department
            order by e.department
            """)
    List<GroupAggregateRow> aggregateByDepartment();

    @Query("""
            select cast(e.level as string) as groupKey, count(e) as headcount,
                   sum(e.baseSalaryUsd) as totalUsd, avg(e.baseSalaryUsd) as avgUsd,
                   min(e.baseSalaryUsd) as minUsd, max(e.baseSalaryUsd) as maxUsd
            from Employee e
            where e.status = com.acme.salary.domain.EmployeeStatus.ACTIVE
            group by e.level
            order by e.level
            """)
    List<GroupAggregateRow> aggregateByLevel();

    /**
     * Normalized (USD) salaries of active employees, ascending. Used to compute
     * median and the distribution histogram in {@code SalaryStatistics}. Returns
     * one scalar per active employee (~10k values) — cheap for a reporting call
     * and keeps percentile math out of vendor-specific SQL.
     */
    @Query("""
            select e.baseSalaryUsd from Employee e
            where e.status = com.acme.salary.domain.EmployeeStatus.ACTIVE
            order by e.baseSalaryUsd asc
            """)
    List<BigDecimal> findActiveSalariesUsdAscending();
}
