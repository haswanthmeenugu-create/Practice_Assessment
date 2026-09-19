package com.acme.salary.seed;

import com.acme.salary.domain.Currency;
import com.acme.salary.domain.Department;
import com.acme.salary.domain.Employee;
import com.acme.salary.domain.EmployeeStatus;
import com.acme.salary.domain.EmploymentType;
import com.acme.salary.domain.Level;
import com.acme.salary.money.CurrencyConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generates realistic-but-synthetic employee records. Pure and deterministic:
 * the same seed always yields the same data, which keeps the seed reproducible
 * and lets the generator be unit-tested without a database.
 *
 * <p>Compensation is anchored to a USD midpoint per level, adjusted by
 * department and country cost factors plus bounded noise, then expressed in the
 * country's local currency via {@link CurrencyConverter}. This keeps the
 * USD-normalized figure comparable across countries while local amounts look
 * plausible.
 */
public class EmployeeGenerator {

    private record CountryInfo(String code, Currency currency, double costFactor) {
    }

    private static final List<CountryInfo> COUNTRIES = List.of(
            new CountryInfo("US", Currency.USD, 1.00),
            new CountryInfo("GB", Currency.GBP, 0.90),
            new CountryInfo("DE", Currency.EUR, 0.88),
            new CountryInfo("FR", Currency.EUR, 0.85),
            new CountryInfo("IE", Currency.EUR, 0.87),
            new CountryInfo("CA", Currency.CAD, 0.82),
            new CountryInfo("AU", Currency.AUD, 0.85),
            new CountryInfo("SG", Currency.SGD, 0.83),
            new CountryInfo("JP", Currency.JPY, 0.80),
            new CountryInfo("IN", Currency.INR, 0.45),
            new CountryInfo("BR", Currency.BRL, 0.50),
            new CountryInfo("ZA", Currency.ZAR, 0.48)
    );

    private static final Map<Level, Integer> USD_MIDPOINT = Map.ofEntries(
            Map.entry(Level.INTERN, 45_000),
            Map.entry(Level.JUNIOR, 70_000),
            Map.entry(Level.MID, 95_000),
            Map.entry(Level.SENIOR, 130_000),
            Map.entry(Level.STAFF, 165_000),
            Map.entry(Level.PRINCIPAL, 200_000),
            Map.entry(Level.MANAGER, 150_000),
            Map.entry(Level.DIRECTOR, 220_000),
            Map.entry(Level.VP, 300_000)
    );

    private static final Map<Department, Double> DEPT_FACTOR = Map.ofEntries(
            Map.entry(Department.ENGINEERING, 1.10),
            Map.entry(Department.DATA, 1.12),
            Map.entry(Department.PRODUCT, 1.08),
            Map.entry(Department.LEGAL, 1.05),
            Map.entry(Department.FINANCE, 1.02),
            Map.entry(Department.SALES, 1.00),
            Map.entry(Department.MARKETING, 0.95),
            Map.entry(Department.OPERATIONS, 0.92),
            Map.entry(Department.HUMAN_RESOURCES, 0.90),
            Map.entry(Department.CUSTOMER_SUPPORT, 0.80)
    );

    private static final Map<Department, String> DEPT_ROLE = Map.ofEntries(
            Map.entry(Department.ENGINEERING, "Software Engineer"),
            Map.entry(Department.DATA, "Data Scientist"),
            Map.entry(Department.PRODUCT, "Product Manager"),
            Map.entry(Department.SALES, "Account Executive"),
            Map.entry(Department.MARKETING, "Marketing Specialist"),
            Map.entry(Department.FINANCE, "Financial Analyst"),
            Map.entry(Department.HUMAN_RESOURCES, "HR Specialist"),
            Map.entry(Department.OPERATIONS, "Operations Specialist"),
            Map.entry(Department.CUSTOMER_SUPPORT, "Support Specialist"),
            Map.entry(Department.LEGAL, "Counsel")
    );

    /** Functional area name used for leadership titles ("Director of Engineering"). */
    private static final Map<Department, String> DEPT_AREA = Map.ofEntries(
            Map.entry(Department.ENGINEERING, "Engineering"),
            Map.entry(Department.DATA, "Data"),
            Map.entry(Department.PRODUCT, "Product"),
            Map.entry(Department.SALES, "Sales"),
            Map.entry(Department.MARKETING, "Marketing"),
            Map.entry(Department.FINANCE, "Finance"),
            Map.entry(Department.HUMAN_RESOURCES, "People"),
            Map.entry(Department.OPERATIONS, "Operations"),
            Map.entry(Department.CUSTOMER_SUPPORT, "Customer Support"),
            Map.entry(Department.LEGAL, "Legal")
    );

    private static final String[] FIRST_NAMES = {
            "Aarav", "Mia", "Liam", "Sofia", "Noah", "Emma", "Lucas", "Olivia", "Kai", "Ava",
            "Ethan", "Isabella", "Arjun", "Chloe", "Mateo", "Zoe", "Hiro", "Lucia", "Omar", "Nina",
            "Diego", "Amara", "Yusuf", "Priya", "Leo", "Hana", "Sami", "Elena", "Tariq", "Freya"
    };

    private static final String[] LAST_NAMES = {
            "Sharma", "Silva", "Chen", "Muller", "Dubois", "Kim", "Rossi", "Okafor", "Nguyen", "Patel",
            "Garcia", "Johnson", "Suzuki", "Ivanov", "Ahmed", "Costa", "Bauer", "Moreau", "Haddad", "Naidoo",
            "Fernandez", "Schmidt", "Rossouw", "Tanaka", "Lopez", "Novak", "Petrov", "Diallo", "Reyes", "Berg"
    };

    private final CurrencyConverter converter;

    public EmployeeGenerator(CurrencyConverter converter) {
        this.converter = converter;
    }

    /**
     * Generate {@code count} employees. Employee codes are assigned as
     * {@code ACME-000001..} in order; emails are made unique with the sequence.
     *
     * @param count number of employees (must be >= 0)
     * @param seed  RNG seed for reproducibility
     */
    public List<Employee> generate(int count, long seed) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }
        Random rnd = new Random(seed);
        Level[] levels = Level.values();
        Department[] departments = Department.values();
        LocalDate today = LocalDate.now();

        List<Employee> employees = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            String first = FIRST_NAMES[rnd.nextInt(FIRST_NAMES.length)];
            String last = LAST_NAMES[rnd.nextInt(LAST_NAMES.length)];
            CountryInfo country = COUNTRIES.get(rnd.nextInt(COUNTRIES.size()));
            Department dept = departments[rnd.nextInt(departments.length)];
            Level level = levels[rnd.nextInt(levels.length)];

            BigDecimal usd = usdSalary(level, dept, country, rnd);
            BigDecimal localSalary = converter.fromBase(usd, country.currency());

            Employee e = new Employee();
            e.setEmployeeCode(String.format("ACME-%06d", i));
            e.setFirstName(first);
            e.setLastName(last);
            e.setEmail(String.format("%s.%s%d@acme.example",
                    first.toLowerCase(), last.toLowerCase(), i));
            e.setCountry(country.code());
            e.setDepartment(dept);
            e.setLevel(level);
            e.setJobTitle(jobTitle(level, dept));
            e.setBaseSalary(localSalary);
            e.setCurrency(country.currency());
            e.setBaseSalaryUsd(usd);
            e.setEmploymentType(employmentType(rnd));
            e.setStatus(status(rnd));
            e.setHireDate(today.minusDays(rnd.nextInt(8 * 365)));
            employees.add(e);
        }
        return employees;
    }

    private static BigDecimal usdSalary(Level level, Department dept, CountryInfo country, Random rnd) {
        double base = USD_MIDPOINT.get(level);
        double factor = DEPT_FACTOR.get(dept) * country.costFactor();
        double noise = 0.85 + rnd.nextDouble() * 0.30; // +/- ~15%
        double value = base * factor * noise;
        // Round to the nearest 500 USD for tidy figures.
        long rounded = Math.round(value / 500.0) * 500L;
        return BigDecimal.valueOf(rounded).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Individual-contributor levels prefix the role ("Senior Data Scientist");
     * leadership levels use the functional area ("Director of Finance").
     */
    private static String jobTitle(Level level, Department dept) {
        String role = DEPT_ROLE.get(dept);
        String area = DEPT_AREA.get(dept);
        return switch (level) {
            case INTERN -> role + " Intern";
            case JUNIOR -> "Junior " + role;
            case MID -> role;
            case SENIOR -> "Senior " + role;
            case STAFF -> "Staff " + role;
            case PRINCIPAL -> "Principal " + role;
            case MANAGER -> area + " Manager";
            case DIRECTOR -> "Director of " + area;
            case VP -> "VP of " + area;
        };
    }

    private static EmploymentType employmentType(Random rnd) {
        int r = rnd.nextInt(100);
        if (r < 88) {
            return EmploymentType.FULL_TIME;
        }
        return r < 95 ? EmploymentType.PART_TIME : EmploymentType.CONTRACT;
    }

    private static EmployeeStatus status(Random rnd) {
        int r = rnd.nextInt(100);
        if (r < 92) {
            return EmployeeStatus.ACTIVE;
        }
        return r < 97 ? EmployeeStatus.ON_LEAVE : EmployeeStatus.TERMINATED;
    }
}
