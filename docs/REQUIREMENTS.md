# Salary Management — Requirements

**Author:** Haswanth Meenugu
**Date:** 2026-09-19
**Status:** v1 (written before implementation)

## Goal

Give the HR Manager of ACME (an org of ~10,000 employees across multiple
countries) a web application to manage employee salary data and answer
questions about **how the organization pays people** — replacing the current
spreadsheet-based workflow.

## Primary user

**HR Manager.** Single trusted internal role. They need to find employees
quickly, correct salary/role data, and understand pay distribution across
countries, departments, and levels.

## Scope (what we build)

1. **Employee & salary records** — create, read, update, delete. Each record
   holds identity (name, email, code), org placement (country, department, job
   title, level), and compensation (base salary + currency, employment type,
   hire date, status).
2. **Find & browse** — server-side pagination, free-text search (name / email /
   code), and filters by country, department, level, and status. At 10,000 rows
   this must not load everything into the browser.
3. **Pay analytics** — the "how do we pay people" view:
   - Headcount and total payroll, normalized to a single base currency (USD).
   - Average and median pay, overall and grouped by country, department, and level.
   - Salary distribution (histogram buckets).
   - Highest/lowest paid groups.
   Multi-currency is normalized via a documented static FX table so cross-country
   comparisons are meaningful.
4. **Seed data** — reproducible script that generates 10,000 realistic employees.

## Out of scope (deliberately) — and why

- **Authentication / RBAC / multi-tenant.** One trusted internal persona was
  specified. Auth is well-understood plumbing that would add surface area
  without demonstrating anything new here. I isolate it behind a single security
  config point so it can be added later without touching business logic.
- **Live currency feeds.** Real-time FX adds an external dependency and
  non-determinism. A versioned static rate table is enough to make pay
  comparisons meaningful and keeps tests deterministic. Swappable behind an
  interface.
- **Salary history / effective-dated changes / audit trail.** Valuable in a real
  HR system, but a significant modeling effort (temporal tables). v1 stores the
  current salary only. The schema leaves room to add a `salary_history` table.
- **Payroll processing, tax, benefits, bonuses/equity.** This is a *management
  and reporting* tool, not a payroll engine. Base salary only.
- **Import/export (Excel/CSV) and bulk edit.** Realistic next step given they
  come from spreadsheets, but not core to demonstrating the design. Noted as
  fast-follow.
- **Internationalized UI / localization.** English-only UI; data spans countries
  but the operator is one HR team.

## Non-functional requirements

- **Performance:** list and analytics endpoints stay responsive at 10k rows.
  Pagination and DB-side aggregation (not in-app loops) are mandatory.
- **Correctness:** money handled with `BigDecimal`, never floating point.
- **Testability:** core logic (analytics math, currency normalization, filtering)
  covered by fast, deterministic tests that need no external services.
- **Maintainability:** clean layering (web → service → repository), DTOs at the
  boundary, entities never leak to the API.

## Tech choices

- **Backend:** Java + Spring Boot (role-aligned), Spring Data JPA.
- **Database:** MySQL 8 (relational; aggregation is a core requirement). H2
  in-memory for tests.
- **Frontend:** Angular + Angular Material.
- **Reasoning:** matches a Java/Angular role, and relational aggregation is the
  natural fit for the analytics questions being asked.

## Success criteria

- HR Manager can find any employee in a few clicks and edit their record.
- The analytics view answers "what do we pay, by country / department / level"
  at a glance.
- Seeding produces 10,000 employees reproducibly.
- Core logic is covered by meaningful, fast tests.
