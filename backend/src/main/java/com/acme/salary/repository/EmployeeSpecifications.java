package com.acme.salary.repository;

import com.acme.salary.domain.Department;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.Level;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Composable filters for employee search. Each factory returns null when its
 * argument is absent, so callers can {@code and()} them freely and unused
 * filters simply drop out of the query.
 */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    /**
     * Case-insensitive match of {@code term} against first name, last name,
     * email, or employee code.
     */
    public static Specification<Employee> matchesText(String term) {
        if (term == null || term.isBlank()) {
            return null;
        }
        String like = "%" + term.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            List<Predicate> ors = new ArrayList<>();
            ors.add(cb.like(cb.lower(root.get("firstName")), like));
            ors.add(cb.like(cb.lower(root.get("lastName")), like));
            ors.add(cb.like(cb.lower(root.get("email")), like));
            ors.add(cb.like(cb.lower(root.get("employeeCode")), like));
            return cb.or(ors.toArray(new Predicate[0]));
        };
    }

    public static Specification<Employee> hasCountry(String country) {
        if (country == null || country.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("country"), country.trim().toUpperCase());
    }

    public static Specification<Employee> hasDepartment(Department department) {
        if (department == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department"), department);
    }

    public static Specification<Employee> hasLevel(Level level) {
        if (level == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("level"), level);
    }

    public static Specification<Employee> hasStatus(EmployeeStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /** Combine specs, ignoring nulls. Returns an always-true spec if all null. */
    public static Specification<Employee> allOf(List<Specification<Employee>> specs) {
        Specification<Employee> result = Specification.where(null);
        for (Specification<Employee> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}
