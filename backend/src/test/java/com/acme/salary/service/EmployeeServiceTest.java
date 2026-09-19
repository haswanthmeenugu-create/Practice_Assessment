package com.acme.salary.service;

import com.acme.salary.domain.Currency;
import com.acme.salary.domain.Department;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.EmploymentType;
import com.acme.salary.domain.Level;
import com.acme.salary.money.StaticRateCurrencyConverter;
import com.acme.salary.web.dto.EmployeeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({EmployeeService.class, StaticRateCurrencyConverter.class})
class EmployeeServiceTest {

    @Autowired
    private EmployeeService service;

    private static EmployeeRequest request(String email, Currency currency, String salary) {
        return new EmployeeRequest(
                "Ada", "Lovelace", email, "GB", Department.ENGINEERING,
                "Senior Software Engineer", Level.SENIOR, new BigDecimal(salary),
                currency, EmploymentType.FULL_TIME, EmployeeStatus.ACTIVE,
                LocalDate.of(2022, 1, 15));
    }

    @Test
    void createGeneratesCodeAndNormalizesToUsd() {
        Employee created = service.create(request("ada@acme.example", Currency.GBP, "79000"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmployeeCode()).matches("ACME-\\d{6}");
        // 79000 GBP at 0.79 GBP/USD = 100000 USD
        assertThat(created.getBaseSalaryUsd()).isEqualByComparingTo("100000.00");
        assertThat(created.getEmail()).isEqualTo("ada@acme.example");
    }

    @Test
    void createRejectsDuplicateEmail() {
        service.create(request("dup@acme.example", Currency.USD, "100000"));
        assertThatThrownBy(() -> service.create(request("dup@acme.example", Currency.USD, "120000")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateChangesSalaryAndRecomputesUsd() {
        Employee created = service.create(request("upd@acme.example", Currency.USD, "100000"));

        EmployeeRequest change = new EmployeeRequest(
                "Ada", "Byron", "upd@acme.example", "US", Department.DATA,
                "Staff Data Scientist", Level.STAFF, new BigDecimal("150000"),
                Currency.USD, EmploymentType.FULL_TIME, EmployeeStatus.ACTIVE,
                LocalDate.of(2021, 6, 1));

        Employee updated = service.update(created.getId(), change);
        assertThat(updated.getLastName()).isEqualTo("Byron");
        assertThat(updated.getBaseSalaryUsd()).isEqualByComparingTo("150000.00");
    }

    @Test
    void getMissingThrowsNotFound() {
        assertThatThrownBy(() -> service.get(999_999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesEmployee() {
        Employee created = service.create(request("del@acme.example", Currency.USD, "90000"));
        service.delete(created.getId());
        assertThatThrownBy(() -> service.get(created.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchFiltersByTextAndCountry() {
        service.create(new EmployeeRequest("Grace", "Hopper", "grace@acme.example", "US",
                Department.ENGINEERING, "Principal Software Engineer", Level.PRINCIPAL,
                new BigDecimal("200000"), Currency.USD, EmploymentType.FULL_TIME,
                EmployeeStatus.ACTIVE, LocalDate.of(2020, 1, 1)));
        service.create(new EmployeeRequest("Alan", "Turing", "alan@acme.example", "GB",
                Department.DATA, "Staff Data Scientist", Level.STAFF,
                new BigDecimal("120000"), Currency.GBP, EmploymentType.FULL_TIME,
                EmployeeStatus.ACTIVE, LocalDate.of(2019, 3, 1)));

        Page<Employee> byText = service.search("hopper", null, null, null, null,
                PageRequest.of(0, 10));
        assertThat(byText.getTotalElements()).isEqualTo(1);
        assertThat(byText.getContent().get(0).getLastName()).isEqualTo("Hopper");

        Page<Employee> byCountry = service.search(null, "gb", null, null, null,
                PageRequest.of(0, 10));
        assertThat(byCountry.getTotalElements()).isEqualTo(1);
        assertThat(byCountry.getContent().get(0).getCountry()).isEqualTo("GB");
    }
}
