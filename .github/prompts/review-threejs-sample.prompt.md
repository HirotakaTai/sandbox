---
description: 'Review a Three.js sample or runtime change for lifecycle, URL, source link, UI, and validation risks.'
name: 'Review Three.js Sample'
agent: 'threejs-sample-reviewer'
tools: [read, search, execute]
argument-hint: 'files, sample id, or change summary to review'
---

# Review Three.js Sample

Review the requested change: `${input:reviewTarget:Describe the files, sample ID, or change summary to review}`.

## Scope

Check architecture, TypeScript strictness, Three.js resource disposal, URL reflection, source code links, sidebar behavior, responsive layout, and validation gaps.

## Workflow

1. Inspect relevant files and current changes before running commands.
2. Run focused validation commands when helpful: `pnpm typecheck`, `pnpm check`, and `pnpm build` for app/runtime changes.
3. Report findings first, ordered by severity, with concrete file references.
4. If no issues are found, state that clearly and list any remaining unverified browser behavior.

## Output Expectations

Use a code-review style response: findings, open questions, validation summary.