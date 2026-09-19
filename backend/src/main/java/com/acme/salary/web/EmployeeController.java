package com.acme.salary.web;

import com.acme.salary.domain.Department;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.Level;
import com.acme.salary.service.EmployeeService;
import com.acme.salary.web.dto.EmployeeRequest;
import com.acme.salary.web.dto.EmployeeResponse;
import com.acme.salary.web.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Set;

/**
 * REST API for employee records: paginated/filtered search plus CRUD.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    /** Cap page size so a client cannot ask for all 10k rows at once. */
    private static final int MAX_PAGE_SIZE = 100;

    private static final Set<String> SORTABLE = Set.of(
            "lastName", "firstName", "country", "department", "level",
            "baseSalaryUsd", "hireDate", "employeeCode");

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Department department,
            @RequestParam(required = false) Level level,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Pageable pageable = toPageable(page, size, sort, direction);
        Page<Employee> result = service.search(q, country, department, level, status, pageable);
        return PageResponse.of(result, EmployeeResponse::from);
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        return EmployeeResponse.from(service.get(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request,
                                                   UriComponentsBuilder uriBuilder) {
        Employee created = service.create(request);
        URI location = uriBuilder.path("/api/employees/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(EmployeeResponse.from(created));
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id,
                                   @Valid @RequestBody EmployeeRequest request) {
        return EmployeeResponse.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private static Pageable toPageable(int page, int size, String sort, String direction) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String safeSort = SORTABLE.contains(sort) ? sort : "lastName";
        Sort.Direction dir = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(dir, safeSort));
    }
}
