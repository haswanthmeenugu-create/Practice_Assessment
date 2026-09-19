package com.acme.salary.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An employee and their current compensation.
 *
 * <p>v1 stores the <em>current</em> salary only. Salary history / effective-dated
 * changes are intentionally out of scope (see docs/REQUIREMENTS.md); the schema
 * leaves room for a separate {@code salary_history} table later.
 *
 * <p>Money is stored as {@link BigDecimal} with the paying {@link Currency}.
 * Cross-currency comparison happens in the analytics layer via normalization.
 */
@Entity
@Table(
        name = "employee",
        indexes = {
                @Index(name = "idx_employee_country", columnList = "country"),
                @Index(name = "idx_employee_department", columnList = "department"),
                @Index(name = "idx_employee_level", columnList = "level"),
                @Index(name = "idx_employee_status", columnList = "status")
        }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable human-facing identifier, e.g. ACME-000042. */
    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String email;

    /** ISO 3166-1 alpha-2 country code, e.g. US, GB, IN. */
    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false, length = 40)
    private Department department;

    @Column(name = "job_title", nullable = false, length = 120)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private Level level;

    @Column(name = "base_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    /**
     * Base salary normalized to the analytics base currency (USD), persisted so
     * grouping/aggregation runs entirely in the database and stays fast at 10k+
     * rows. Maintained by the service layer whenever salary or currency changes;
     * never set directly by callers.
     */
    @Column(name = "base_salary_usd", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseSalaryUsd;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    public Employee() {
        // for JPA and construction by the service layer
    }

    // Getters / setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBaseSalaryUsd() {
        return baseSalaryUsd;
    }

    public void setBaseSalaryUsd(BigDecimal baseSalaryUsd) {
        this.baseSalaryUsd = baseSalaryUsd;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
