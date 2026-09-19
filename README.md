# ACME Salary Management

Web application for an HR Manager to manage salary data for ~10,000 employees
across multiple countries and answer "how does the org pay people".

Built for the *Assessment with Product Framing* take-home.

| | |
|---|---|
| Backend | Java 21 bytecode (built on JDK 25), Spring Boot 3.3, Spring Data JPA |
| Database | MySQL 8 at runtime, H2 in-memory for tests |
| Frontend | Angular 18 (standalone components), Angular Material |
| Seed | 10,000 deterministic employees, loaded on first start |

## Artifacts

- [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) — goal, scope, deliberate exclusions (written before code)
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — layering, trade-offs, performance, testing strategy
- [docs/AI-USAGE.md](docs/AI-USAGE.md) — how AI tools were used and where judgment was applied
- Commit history — incremental, one layer per commit

## Run it locally

### 1. Database

MySQL 8 running on `localhost:3306`. The app creates the `salary_management`
database and schema on first start. Credentials come from the environment:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<your mysql password>"
# optional: $env:DB_URL = "jdbc:mysql://host:3306/salary_management?createDatabaseIfNotExist=true&serverTimezone=UTC"
```

### 2. Backend

```powershell
cd backend
mvn spring-boot:run
```

- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- On first start with an empty table, 10,000 employees are seeded
  (`app.seed.enabled` / `app.seed.count` in `application.properties`).

### 3. Frontend

```powershell
cd frontend
npm install
npm start          # http://localhost:4200, proxies /api to :8080
```

## Tests

```powershell
cd backend
mvn test           # 30 tests, H2 in-memory, no MySQL needed, ~20s
```

```powershell
cd frontend
npm test           # Karma/Jasmine unit tests
```

## API at a glance

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/employees?q=&country=&department=&level=&status=&page=&size=&sort=&direction=` | Paginated, filtered search |
| GET | `/api/employees/{id}` | Fetch one |
| POST | `/api/employees` | Create (server assigns `ACME-######` code) |
| PUT | `/api/employees/{id}` | Update |
| DELETE | `/api/employees/{id}` | Delete |
| GET | `/api/analytics/overview` | Headcount, total payroll, avg/median/min/max (USD) |
| GET | `/api/analytics/by/{country\|department\|level}` | Pay breakdown by dimension (USD) |
| GET | `/api/analytics/distribution` | Salary histogram (USD) |

All analytics figures are normalized to USD via a static FX table so
cross-country comparisons are meaningful. See ARCHITECTURE.md for why.

## Project layout

```
backend/   Spring Boot service (web -> service -> repository), seed, tests
frontend/  Angular SPA: employees/ (table + form), analytics/ (dashboard), core/ (API clients)
docs/      Requirements, architecture, AI usage
```
