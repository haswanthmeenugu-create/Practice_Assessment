package com.acme.salary.seed;

import com.acme.salary.domain.Employee;
import com.acme.salary.money.CurrencyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Seeds the database with synthetic employees on startup when the table is
 * empty. Uses a single JDBC batch insert (not per-row Hibernate saves) so
 * loading 10,000 rows is fast; the IDENTITY key strategy would otherwise
 * disable Hibernate's batch inserts.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final long SEED = 20260919L;
    private static final int BATCH_SIZE = 1000;

    private static final String INSERT_SQL = """
            insert into employee
              (employee_code, first_name, last_name, email, country, department,
               job_title, level, base_salary, currency, base_salary_usd,
               employment_type, status, hire_date)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final CurrencyConverter currencyConverter;
    private final boolean enabled;
    private final int count;

    public DataSeeder(JdbcTemplate jdbcTemplate,
                      CurrencyConverter currencyConverter,
                      @Value("${app.seed.enabled:true}") boolean enabled,
                      @Value("${app.seed.count:10000}") int count) {
        this.jdbcTemplate = jdbcTemplate;
        this.currencyConverter = currencyConverter;
        this.enabled = enabled;
        this.count = count;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Seeding disabled (app.seed.enabled=false)");
            return;
        }
        Long existing = jdbcTemplate.queryForObject("select count(*) from employee", Long.class);
        if (existing != null && existing > 0) {
            log.info("Employee table already has {} rows; skipping seed", existing);
            return;
        }

        long start = System.currentTimeMillis();
        List<Employee> employees = new EmployeeGenerator(currencyConverter).generate(count, SEED);
        insertInBatches(employees);
        log.info("Seeded {} employees in {} ms", employees.size(), System.currentTimeMillis() - start);
    }

    private void insertInBatches(List<Employee> employees) {
        for (int start = 0; start < employees.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, employees.size());
            List<Employee> chunk = employees.subList(start, end);
            jdbcTemplate.batchUpdate(INSERT_SQL, chunk, chunk.size(), (ps, e) -> bind(ps, e));
        }
    }

    private static void bind(PreparedStatement ps, Employee e) throws java.sql.SQLException {
        ps.setString(1, e.getEmployeeCode());
        ps.setString(2, e.getFirstName());
        ps.setString(3, e.getLastName());
        ps.setString(4, e.getEmail());
        ps.setString(5, e.getCountry());
        ps.setString(6, e.getDepartment().name());
        ps.setString(7, e.getJobTitle());
        ps.setString(8, e.getLevel().name());
        ps.setBigDecimal(9, e.getBaseSalary());
        ps.setString(10, e.getCurrency().name());
        ps.setBigDecimal(11, e.getBaseSalaryUsd());
        ps.setString(12, e.getEmploymentType().name());
        ps.setString(13, e.getStatus().name());
        ps.setDate(14, Date.valueOf(e.getHireDate()));
    }
}
