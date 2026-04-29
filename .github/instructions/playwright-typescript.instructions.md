---
description: 'Use when writing Playwright TypeScript tests for the sample browser, sidebar navigation, URL behavior, canvas rendering, or accessibility flows.'
applyTo: 'tests/**/*.ts, e2e/**/*.ts, playwright.config.ts'
---

# Playwright TypeScript Testing

These instructions adapt the awesome-copilot `playwright-typescript.instructions.md` guidance for this Three.js sample browser.

## Test Writing Guidelines

- Prefer user-facing, role-based locators such as `getByRole`, `getByLabel`, and `getByText`.
- Use `test.step()` to group meaningful interactions and make reports easier to read.
- Use auto-retrying web-first assertions with `await expect(...)`.
- Avoid hard-coded waits and custom timeout increases. Rely on Playwright auto-waiting.
- Use descriptive test and step titles that state the user-facing behavior.
- Add comments only for non-obvious setup or visual verification logic.

## Sample Browser Coverage

- Verify that the default sample renders and that the main canvas is present.
- Verify sidebar open/close behavior, active item state, and keyboard-usable navigation.
- Verify that selecting a sample updates the URL and that direct URLs restore the selected sample.
- Verify that the active sample exposes a source code link.
- For Three.js canvas checks, combine DOM assertions with a lightweight nonblank rendering check when practical.

## Test Structure

- Start test files with `import { expect, test } from "@playwright/test";`.
- Group related tests with `test.describe()`.
- Use `beforeEach` for repeated page navigation or setup.
- Store end-to-end tests in `tests/` or `e2e/` using `<feature-or-page>.spec.ts` naming.

## Assertion Preferences

- Use `toHaveURL` for URL reflection.
- Use `toHaveCount`, `toHaveText`, and `toContainText` for UI state.
- Use accessibility snapshots or role assertions for navigation and sidebar structure.
- Avoid asserting implementation details that would make learning-sample refactors brittle.

## Execution Strategy

1. Run tests in Chromium first.
2. Diagnose failures from the user-visible behavior and page snapshot.
3. Refine locators before changing application code.
4. Re-run the focused failing test, then the full relevant test file.