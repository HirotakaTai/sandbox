---
description: 'Add or update a Three.js learning sample using this project architecture and validation workflow.'
name: 'Add Three.js Sample'
agent: 'threejs-sample-builder'
tools: [read, edit, search, execute, todo]
argument-hint: 'sample id, concept, visible behavior, difficulty'
---

# Add Three.js Sample

Implement the requested sample or sample change: `${input:sampleBrief:Describe the sample concept, behavior, and difficulty}`.

## Mission

Add a focused Three.js learning sample that works inside this repository's sample browser architecture.

## Workflow

1. Inspect the current `src/` structure, sample registry, runtime, and docs before editing.
2. Confirm or derive a kebab-case sample ID, display name, description, tags, difficulty, and source path.
3. Implement the sample with lifecycle behavior equivalent to `init`, `update`, and `dispose`.
4. Wire it into navigation, URL reflection, and active sample source links.
5. Add or update concise sample documentation under `docs/samples/` when that structure exists.
6. Run `pnpm typecheck` and `pnpm check`; run `pnpm build` when app startup, routing, runtime, or config changed.

## Output Expectations

Summarize changed files, sample behavior, validation results, and any visual checks that still need browser verification.