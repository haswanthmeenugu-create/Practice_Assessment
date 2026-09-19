package com.acme.salary.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.salary.domain.Currency;
import com.acme.salary.domain.Department;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.EmploymentType;
import com.acme.salary.domain.Level;
import com.acme.salary.web.dto.EmployeeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API test through the full Spring context against H2. No mocking:
 * exercises controller, validation, service, and persistence together.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static EmployeeRequest valid(String email) {
        return new EmployeeRequest(
                "Grace", "Hopper", email, "US", Department.ENGINEERING,
                "Principal Software Engineer", Level.PRINCIPAL, new BigDecimal("200000"),
                Currency.USD, EmploymentType.FULL_TIME, EmployeeStatus.ACTIVE,
                LocalDate.of(2020, 1, 1));
    }

    @Test
    void createThenFetchEmployee() throws Exception {
        String body = objectMapper.writeValueAsString(valid("create@acme.example"));

        String location = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").exists())
                .andExpect(jsonPath("$.baseSalaryUsd").value(200000.00))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("create@acme.example"));
    }

    @Test
    void validationFailureReturns400WithFieldErrors() throws Exception {
        EmployeeRequest invalid = new EmployeeRequest(
                "", "Hopper", "not-an-email", "USA", Department.ENGINEERING,
                "Engineer", Level.MID, new BigDecimal("-5"),
                Currency.USD, EmploymentType.FULL_TIME, EmployeeStatus.ACTIVE,
                LocalDate.of(2020, 1, 1));
        String body = objectMapper.writeValueAsString(invalid);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void fetchingMissingEmployeeReturns404() throws Exception {
        mockMvc.perform(get("/api/employees/987654"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void analyticsOverviewIsReachable() throws Exception {
        mockMvc.perform(get("/api/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("USD"));
    }

    @Test
    void unknownAnalyticsDimensionReturns400() throws Exception {
        mockMvc.perform(get("/api/analytics/by/planet"))
                .andExpect(status().isBadRequest());
    }
}
