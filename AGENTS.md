# Agent instructions for Halls of 7800

## Production code

- Change production code only when the user explicitly asks for an implementation or explicitly approves a proposed change. A request to implement a named feature or fix a specific bug grants permission for that work.
- Production code includes everything under `src/main/` and any build or configuration changes that affect the shipped game or its runtime behavior. This rule also applies to refactors, formatting, generated changes, and automated fixes in those files.
- Reviews, baselines, bug investigations, coverage work, and requests limited to tests or documentation do not grant permission to edit production code. Inspect the code, run relevant checks, and report findings. If a production edit is needed to finish the request, explain the proposed edit and wait for explicit approval before making it.

## Working in this repository

- Keep edits within the requested scope and preserve existing user changes. If a test exposes a bug, report it and leave the assertion intact.
- For approved gameplay changes, check the impact on seeded generation and save and replay behavior when relevant.
- Summarize the files changed, the checks run, and any verification gaps.

## Testing and CI

Run the relevant Maven checks after code or test edits.

- Run `.\mvnw.cmd test` on Windows or `bash ./mvnw test` on macOS and Linux for JUnit and the JaCoCo report.
- Run the full quality build with `.\mvnw.cmd -B clean verify -Pquality` on Windows or `bash ./mvnw -B clean verify -Pquality` on macOS and Linux. JaCoCo enforces project-wide minimums of 30% line coverage and 20% branch coverage. PIT and SpotBugs publish findings for review.
- Look for JaCoCo HTML and XML in `target/site/jacoco/`, PIT results in `target/pit-reports/`, SpotBugs findings in `target/spotbugsXml.xml`, and JUnit results in `target/surefire-reports/`.
- GitHub Actions runs the full quality build on pushes and pull requests with Temurin 17 on Ubuntu. It invokes the wrapper through Bash, uses read-only repository access, shows coverage in the run summary, and uploads the available reports as `quality-reports`.
