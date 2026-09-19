# Architecture & Design Notes

## System overview

```
+---------------------+        HTTP/JSON         +----------------------------+        JDBC        +-----------+
|  Angular 18 SPA     |  ------------------->    |  Spring Boot 3 REST API    |  -------------->   |  MySQL 8  |
|  Angular Material   |  /api/employees          |  web -> service -> repo    |                    |  employee |
|  (frontend/)        |  /api/analytics/*        |  (backend/)                |                    |           |
+---------------------+                          +----------------------------+                    +-----------+
                                                          |
                                                          +-- H2 in-memory (tests only)
```

One backend service, one table, one SPA. Deliberately small: the assessment
asks for judgment, not a distributed system.

## Backend layering

| Layer | Package | Responsibility |
|---|---|---|
| Web | `web`, `web.dto` | REST endpoints, validation, error mapping, pagination envelope. Entities never cross this boundary. |
| Service | `service` | Business rules: uniqueness, USD normalization invariant, analytics orchestration. Transaction boundary. |
| Analytics math | `analytics` | Pure functions (median, histogram). No Spring, no DB. |
| Money | `money` | `CurrencyConverter` interface + static-rate implementation. |
| Persistence | `repository`, `domain` | JPA entity, Spring Data repository, `Specification` filters, GROUP BY projections. |
| Seed | `seed` | Deterministic generator + JDBC batch loader. |

## Key decisions and trade-offs

### 1. Persisted `base_salary_usd` column
**Decision:** store the USD-normalized salary on each row, maintained by the
service whenever salary or currency changes.
**Why:** the analytics questions ("average pay by country", "total payroll")
are aggregations across currencies. Normalizing at write time means every
aggregate is a plain `GROUP BY ... SUM/AVG` in the database — one indexed query
per breakdown, no per-row conversion in application memory.
**Cost:** if FX rates change, existing rows must be re-normalized (a batch
job). Acceptable for a static-rate v1; documented as the migration path if a
live feed is introduced.

### 2. Median and histogram computed in Java, not SQL
**Decision:** fetch one ordered column of active USD salaries (~10k decimals)
and compute median/buckets in `SalaryStatistics`.
**Why:** percentile SQL is vendor-specific (MySQL lacks `MEDIAN`; H2 has it),
which would break the "tests run on H2, prod on MySQL" setup. Pulling a single
numeric column for a reporting call is cheap, and the math becomes trivially
unit-testable.
**Cost:** O(n) transfer for the overview/distribution calls. Fine at 10k;
at 1M+ we would move to a pre-aggregated table or approximate percentiles.

### 3. Dynamic filtering via JPA `Specification`
**Why:** the list screen combines free text + up to four optional filters.
Specifications compose cleanly; each absent filter drops out of the query.
The alternative (one repository method per filter combination) does not scale.

### 4. JDBC batch insert for seeding
**Why:** the entity uses `IDENTITY` ids, which disables Hibernate's insert
batching. Seeding 10k rows one `INSERT` at a time is slow and noisy. A
`JdbcTemplate.batchUpdate` in 1,000-row chunks keeps seeding to a few seconds
and avoids the persistence context altogether.

### 5. Hibernate `ddl-auto=update` instead of Flyway (v1)
**Why:** one table, one developer, one deploy. Auto-DDL keeps the H2 test
schema and MySQL runtime schema derived from the same source (the entity), so
they cannot drift.
**Production path:** switch to Flyway with a baseline migration. Explicitly a
v1 shortcut, not a recommendation.

### 6. Static FX rates behind an interface
**Why:** deterministic, testable, no external calls. The `CurrencyConverter`
interface is the seam for a real provider later.

### 7. Server-generated employee code derived from the id
**Why:** a stable, human-friendly identifier (`ACME-000042`) that HR can quote.
Deriving from the DB id guarantees uniqueness and lines up with the seed
(`ACME-000001..ACME-010000`). Create is a two-step insert-then-set inside one
transaction; a temporary placeholder satisfies the NOT NULL/UNIQUE constraint.

### 8. No authentication (v1)
Single trusted internal persona. `WebConfig` is the single place to add
Spring Security later without touching controllers or services.

## Performance considerations (10k employees)

- **List:** server-side pagination, page size capped at 100, sort whitelist.
  Indexes on `country`, `department`, `level`, `status`.
- **Search:** `LIKE %term%` across four columns. Fine at 10k; at scale we
  would add a full-text index or a search service.
- **Analytics:** grouped aggregates are single indexed queries on the
  pre-normalized column. Overview/distribution transfer one scalar per active
  employee.
- **Seed:** batched JDBC, ~10k rows in low single-digit seconds locally.

## Testing strategy

| Kind | Tool | What it proves |
|---|---|---|
| Pure unit | JUnit 5 + AssertJ | Statistics math, FX conversion, generator determinism |
| JPA slice | `@DataJpaTest` on H2 | Service rules and real GROUP BY queries |
| API integration | `@SpringBootTest` + MockMvc | Wiring, validation, error shapes, JSON contract |

No mocking framework is used. Tests need no external database and run in
under 20 seconds.

## Frontend structure

Angular 18 standalone components with Angular Material.

- `employees/` — paginated table (server-side), filter bar, create/edit dialog.
- `analytics/` — overview KPI tiles, breakdown table by country/department/level,
  salary distribution chart.
- `core/` — typed API client (`EmployeeApi`, `AnalyticsApi`), models, error
  interceptor.

The UI never computes analytics itself; it renders what the API returns.

## What I would do next

1. Flyway migrations and a CI pipeline running the test suite.
2. Salary history table with effective dates; audit trail on edits.
3. CSV import/export (they are migrating from spreadsheets).
4. Authentication and role-based access.
5. Live FX provider with a cached rate snapshot and a re-normalization job.
