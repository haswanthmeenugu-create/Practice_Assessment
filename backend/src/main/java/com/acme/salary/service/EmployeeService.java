package com.acme.salary.service;

import com.acme.salary.domain.Department;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.Level;
import com.acme.salary.money.CurrencyConverter;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.EmployeeSpecifications;
import com.acme.salary.web.dto.EmployeeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business operations for employees: CRUD plus filtered, paginated search.
 * Owns the invariant that {@code baseSalaryUsd} always reflects the current
 * salary and currency, and that email / employee code stay unique.
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;
    private final CurrencyConverter currencyConverter;

    public EmployeeService(EmployeeRepository repository, CurrencyConverter currencyConverter) {
        this.repository = repository;
        this.currencyConverter = currencyConverter;
    }

    @Transactional(readOnly = true)
    public Page<Employee> search(String text,
                                 String country,
                                 Department department,
                                 Level level,
                                 EmployeeStatus status,
                                 Pageable pageable) {
        Specification<Employee> spec = EmployeeSpecifications.allOf(java.util.Arrays.asList(
                EmployeeSpecifications.matchesText(text),
                EmployeeSpecifications.hasCountry(country),
                EmployeeSpecifications.hasDepartment(department),
                EmployeeSpecifications.hasLevel(level),
                EmployeeSpecifications.hasStatus(status)
        ));
        return repository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Employee get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    public Employee create(EmployeeRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        Employee employee = new Employee();
        // Temporary unique code so the NOT NULL/UNIQUE constraint holds on first
        // insert; replaced with a stable ACME-###### code derived from the id.
        employee.setEmployeeCode("TMP-" + UUID.randomUUID());
        apply(request, employee);
        Employee saved = repository.saveAndFlush(employee);
        saved.setEmployeeCode(formatCode(saved.getId()));
        return repository.save(saved);
    }

    public Employee update(Long id, EmployeeRequest request) {
        Employee employee = get(id);
        if (!employee.getEmail().equalsIgnoreCase(request.email())
                && repository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use: " + request.email());
        }
        apply(request, employee);
        return repository.save(employee);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found: " + id);
        }
        repository.deleteById(id);
    }

    /** Copy request fields onto the entity and recompute the USD-normalized salary. */
    private void apply(EmployeeRequest r, Employee e) {
        e.setFirstName(r.firstName().trim());
        e.setLastName(r.lastName().trim());
        e.setEmail(r.email().trim().toLowerCase());
        e.setCountry(r.country().trim().toUpperCase());
        e.setDepartment(r.department());
        e.setJobTitle(r.jobTitle().trim());
        e.setLevel(r.level());
        e.setBaseSalary(r.baseSalary());
        e.setCurrency(r.currency());
        e.setBaseSalaryUsd(currencyConverter.toBase(r.baseSalary(), r.currency()));
        e.setEmploymentType(r.employmentType());
        e.setStatus(r.status());
        e.setHireDate(r.hireDate());
    }

    private static String formatCode(long id) {
        return String.format("ACME-%06d", id);
    }

    private static Specification<Employee> nullSafe(Specification<Employee> spec) {
        return spec;
    }
}
