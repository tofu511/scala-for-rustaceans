# Repository Guidelines

## Project Structure & Module Organization
- Top-level chapters live in `chapter-*/` (e.g., `chapter-02-fundamentals`). Each chapter is its own sbt project with `build.sbt`, `README.md`, and code under `src/main/scala`.
- Tests (when present) live in `src/test/scala` inside the chapter directory.
- Exercises and solutions appear under `src/main/scala/exercises/` and `src/main/scala/exercises/solutions/` in some chapters.
- Shared docs live in `docs/` (glossary, comparisons, resources).
- The initial hello-world project is in `chapter-01-setup/`.

## Build, Test, and Development Commands
Run these from a chapter directory that contains a `build.sbt`:
- `sbt compile` — compile the chapter sources.
- `sbt run` — run the main app for that chapter (if defined).
- `sbt test` — run ScalaTest-based test suites.
- `sbt console` — open a REPL with project classes on the classpath.
- `sbt ~compile` — recompile on file changes.
- `sbt clean` — remove build artifacts.

## Coding Style & Naming Conventions
- Indentation: 2 spaces (follow existing Scala files).
- File naming often uses numeric prefixes + snake_case (e.g., `02_pattern_matching.scala`).
- Scala objects/classes use PascalCase; packages are lowercase (e.g., `fundamentals`).
- Keep whitespace clean: no tabs, no trailing spaces, and use blank lines to separate major sections.
- Compiler flags are defined per chapter in `build.sbt` (e.g., `-Xfatal-warnings` in some chapters). Treat warnings as errors where enabled and match the local chapter style.
- There is no project-wide formatter configuration. If you add one, use Scalafmt and place `.scalafmt.conf` at the repo root so chapters stay consistent.

## Testing Guidelines
- Testing framework: ScalaTest (with Cats Effect and Doobie helpers in later chapters).
- Tests are named `*Spec.scala` and live under `src/test/scala`.
- Run tests with `sbt test` from the chapter directory. There is no explicit coverage target.

## Commit & Pull Request Guidelines
- Commit messages in history use an imperative summary with chapter context, e.g., `Add Chapter 09: http4s - routes, JSON, middleware, typed errors`.
- Keep commit subjects short and descriptive; include the chapter number when applicable.
- PRs should include a concise summary, relevant chapter(s), and test results (`sbt test`) when tests exist. Link issues if applicable.

## Environment & Tooling
- Recommended toolchain: Java 17, Scala 2.13.13, sbt 1.9.x (see `README.md` for SDKMAN/mise setup).
