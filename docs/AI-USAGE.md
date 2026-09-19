# How AI Was Used

This project was built with Claude Code (Anthropic's agentic CLI) as the
primary implementation tool, directed by me. This note records how it was used
and where human judgment shaped the result, as the assessment asks.

## Working model

- **I set direction; the agent produced code.** I chose the stack (Spring Boot,
  Angular, MySQL), the scope (CRUD + pay analytics, no auth), and the
  non-goals. The agent proposed designs, I accepted or redirected.
- **Requirements first.** The agent was instructed to read the assessment brief
  and produce `docs/REQUIREMENTS.md` before writing any code, then commit it
  alone so the history shows the plan preceding the implementation.
- **Incremental commits.** Each layer (domain, service, web, seed, tests,
  frontend, docs) was committed separately with a descriptive message so the
  evolution is reviewable.
- **Verify, don't trust.** Every backend change was compiled and the full test
  suite run locally. The first test run surfaced a real bug (see below) which
  was fixed before committing.

## Representative prompts / instructions

Paraphrased from the session:

1. "Read the assessment PDF and Word doc in Downloads. Summarize what they ask
   for. We are going with Spring Boot + Angular + MySQL."
2. "Write a one-page requirements doc first: goal, scope, what we deliberately
   leave out and why. Commit it before any code."
3. "Build the domain model. Salaries are multi-currency; make cross-country
   comparison meaningful. Money must be BigDecimal."
4. "Analytics must stay fast at 10k rows. Aggregate in the database, not in
   application loops. Keep SQL portable so tests run on H2 and prod on MySQL."
5. "Seed 10,000 employees reproducibly. Use batch inserts."
6. "Write meaningful tests: pure unit tests for the math, JPA slice tests for
   queries, one full-stack API test. Fast and deterministic, no external DB."
7. "Run the tests. Fix whatever fails."
8. "Scaffold Angular 18 with Angular Material. Employee list with server-side
   paging and filters, create/edit dialog, analytics dashboard."

## Where judgment was applied

- **Normalized USD column vs. convert-on-read.** The agent initially proposed
  converting in the service layer. I pushed for a persisted normalized column
  so every analytics query is a plain indexed `GROUP BY`. Trade-off recorded in
  `docs/ARCHITECTURE.md`.
- **Median in Java, not SQL.** Vendor differences (MySQL vs. H2) would have
  broken the test strategy. Chose portability plus a pure, tested helper.
- **Two-phase employee code generation.** First test run failed: the temporary
  placeholder code overflowed the column. Fixed by shortening the placeholder
  and giving the column modest headroom rather than widening it to fit a UUID.
- **No Lombok, no Mockito.** The machine runs JDK 25; bytecode-generation
  libraries lag new JDKs. Plain Java records and real Spring slices avoided a
  class of flaky tooling problems and kept tests honest.
- **Removed dead indirection.** A pointless `nullSafe()` wrapper the agent
  wrote was deleted during review; the composition helper already skipped
  nulls.

## Verification loop

Nothing was accepted on the agent's word. Each stage was checked by running it:

1. **Backend tests after every layer.** The first full run failed six tests;
   the cause (placeholder code too long for its column) was fixed and re-run
   to 30/30 green before commit.
2. **Frontend build + headless unit tests** before the frontend commit. A
   missing pipe import surfaced at compile time and was fixed. The Material
   schematic silently failed on a flag; the theme, fonts and providers were
   wired by hand and verified in the build output.
3. **End-to-end smoke run.** The backend was started against H2 with seeding
   on, the Angular dev server pointed at it, and both screens screenshotted
   with headless Chrome (`docs/screenshots/`). Seeding 10,000 rows took ~1 s.
   The screenshots exposed two cosmetic problems (wrapping columns, awkward
   seeded job titles) which were then fixed.
4. **Environment issues diagnosed, not guessed.** Two startup failures during
   the smoke run were traced via logs to (a) another service on port 8080 and
   (b) main properties overriding test properties, rather than retried blindly.

The smoke run led to a product decision: a `demo` Spring profile so anyone
can run the full system with no database setup.

## What the AI did not decide

Stack, scope boundaries, what to exclude, database choice, the decision to
skip authentication, and the acceptance of every trade-off above were mine.
