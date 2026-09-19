package com.acme.salary.seed;

import com.acme.salary.domain.Employee;
import com.acme.salary.money.StaticRateCurrencyConverter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeGeneratorTest {

    private final EmployeeGenerator generator =
            new EmployeeGenerator(new StaticRateCurrencyConverter());

    @Test
    void generatesRequestedCount() {
        assertThat(generator.generate(250, 1L)).hasSize(250);
    }

    @Test
    void isDeterministicForTheSameSeed() {
        List<Employee> first = generator.generate(50, 42L);
        List<Employee> second = generator.generate(50, 42L);

        assertThat(codes(first)).isEqualTo(codes(second));
        assertThat(first.get(0).getEmail()).isEqualTo(second.get(0).getEmail());
        assertThat(first.get(10).getBaseSalary()).isEqualByComparingTo(second.get(10).getBaseSalary());
    }

    @Test
    void assignsSequentialCodesAndUniqueEmails() {
        List<Employee> employees = generator.generate(500, 7L);

        assertThat(employees.get(0).getEmployeeCode()).isEqualTo("ACME-000001");
        assertThat(employees.get(499).getEmployeeCode()).isEqualTo("ACME-000500");

        long distinctEmails = employees.stream().map(Employee::getEmail).distinct().count();
        assertThat(distinctEmails).isEqualTo(employees.size());
    }

    @Test
    void normalizedUsdSalaryIsConsistentWithLocalSalary() {
        StaticRateCurrencyConverter converter = new StaticRateCurrencyConverter();
        for (Employee e : generator.generate(200, 99L)) {
            assertThat(converter.toBase(e.getBaseSalary(), e.getCurrency()))
                    .as("USD normalization for %s", e.getEmployeeCode())
                    .isEqualByComparingTo(e.getBaseSalaryUsd());
        }
    }

    private static List<String> codes(List<Employee> employees) {
        return employees.stream().map(Employee::getEmployeeCode).collect(Collectors.toList());
    }
}
