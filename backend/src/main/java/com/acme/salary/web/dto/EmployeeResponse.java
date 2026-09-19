package com.acme.salary.web.dto;

import com.acme.salary.domain.Currency;
import com.acme.salary.domain.Department;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.EmploymentType;
import com.acme.salary.domain.Level;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Output view of an employee. Decoupled from the JPA entity so persistence
 * concerns never leak to the API.
 */
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String country,
        Department department,
        String jobTitle,
        Level level,
        BigDecimal baseSalary,
        Currency currency,
        BigDecimal baseSalaryUsd,
        EmploymentType employmentType,
        EmployeeStatus status,
        LocalDate hireDate
) {
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(
                e.getId(),
                e.getEmployeeCode(),
                e.getFirstName(),
                e.getLastName(),
                e.getEmail(),
                e.getCountry(),
                e.getDepartment(),
                e.getJobTitle(),
                e.getLevel(),
                e.getBaseSalary(),
                e.getCurrency(),
                e.getBaseSalaryUsd(),
                e.getEmploymentType(),
                e.getStatus(),
                e.getHireDate()
        );
    }
}
