---
name: threejs-sample-workflow
description: 'Workflow for adding, refactoring, or reviewing Three.js learning samples in this Vite TypeScript project. Use when asked to create a sample, adjust sample runtime lifecycle, sidebar routing, URL reflection, source code links, docs, or validation.'
argument-hint: 'sample id or feature request'
---

# Three.js Sample Workflow

Use this skill for repeatable sample work in this repository.

## When to Use This Skill

- The user asks to add, update, or review a Three.js sample.
- The task touches sample metadata, routing, URL state, source links, or docs.
- The task changes runtime lifecycle, animation behavior, disposal, controls, textures, lighting, or postprocessing.
- The task asks for a self-review after implementation.

## Workflow

1. Read `.github/copilot-instructions.md` and relevant `.github/instructions/*.instructions.md` files.
2. Inspect current `src/`, `docs/samples/`, and existing sample patterns before editing.
3. Define or verify the sample contract: ID, display name, description, tags, difficulty, source path, lifecycle functions, and documentation path.
4. Implement the smallest coherent change that keeps app shell, routing, runtime, Three.js helpers, and sample modules separated.
5. Ensure the active sample is reflected in the URL and that source code links are visible in the UI.
6. Add cleanup for every Three.js resource, event listener, and control created by the sample.
7. Run `pnpm typecheck` and `pnpm check`; run `pnpm build` for app startup, routing, runtime, or configuration changes.
8. Self-review for missing TSDoc, non-null assertions, incomplete disposal, layout risks, and unverified browser behavior.

## Acceptance Checklist

- Sample can be selected from the sidebar.
- URL reflects the active sample and direct links work.
- Active sample shows a source code link.
- Lifecycle cleanup is explicit and safe across sample switches.
- TypeScript and Biome validation pass, or remaining warnings are explained.
- Documentation exists or the reason for deferring it is explicit.

## Gotchas

- Do not create one animation loop per sample. Keep app-level rendering centralized.
- Do not add source links as an afterthought; include source paths in metadata from the start.
- Do not treat Three.js disposal as optional in examples. Learning samples should model good cleanup habits.
- Do not hide requirements in prose-only docs; wire them into metadata, UI, and runtime behavior.