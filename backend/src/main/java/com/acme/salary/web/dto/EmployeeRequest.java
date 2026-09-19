package com.acme.salary.web.dto;

import com.acme.salary.domain.Currency;
import com.acme.salary.domain.Department;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.EmploymentType;
import com.acme.salary.domain.Level;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input payload for creating or updating an employee. The employee code is
 * server-generated on create and immutable thereafter, so it is not part of the
 * request. USD normalization is derived, never accepted from the client.
 */
public record EmployeeRequest(

        @NotBlank @Size(max = 80) String firstName,

        @NotBlank @Size(max = 80) String lastName,

        @NotBlank @Email @Size(max = 160) String email,

        @NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "country must be a 2-letter code")
        String country,

        @NotNull Department department,

        @NotBlank @Size(max = 120) String jobTitle,

        @NotNull Level level,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "baseSalary must be positive")
        @Digits(integer = 13, fraction = 2)
        BigDecimal baseSalary,

        @NotNull Currency currency,

        @NotNull EmploymentType employmentType,

        @NotNull EmployeeStatus status,

        @NotNull @PastOrPresent LocalDate hireDate
) {
}
