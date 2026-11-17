# Agent instructions for Halls of 7800

## Production code

- Change production code only when the user explicitly asks for an implementation or explicitly approves a proposed change. A request to implement a named feature or fix a specific bug grants permission for that work.
- Production code includes everything under `src/main/` and any build or configuration changes that affect the shipped game or its runtime behavior. This rule also applies to refactors, formatting, generated changes, and automated fixes in those files.
- Reviews, baselines, bug investigations, coverage work, and requests limited to tests or documentation do not grant permission to edit production code. Inspect the code, run relevant checks, and report findings. If a production edit is needed to finish the request, explain the proposed edit and wait for explicit approval before making it.

## Working in this repository

- Keep edits within the requested scope and preserve existing user changes. If a test exposes a bug, report it and leave the assertion intact.
- For approved gameplay changes, check the impact on seeded generation and save and replay behavior when relevant.
- Run the relevant Maven checks after code or test edits. Use `.\mvnw.cmd test` on Windows or `bash ./mvnw test` on macOS and Linux. The local JaCoCo report is written to `target/site/jacoco/`.
- Summarize the files changed, the checks run, and any verification gaps.
