# Copilot Project Instructions

This repository is a local learning sandbox for verifying multiple Three.js examples in a Vite, TypeScript, and Biome project. The app must present a collapsible left sidebar of samples and a main panel where the selected sample runs.

## Project Priorities

- Keep the implementation framework-free: use Vite, TypeScript, DOM APIs, and Three.js. Do not introduce React or another UI framework unless the user explicitly changes the architecture.
- Preserve the learning purpose. Samples should be small, readable, and focused on one Three.js concept at a time.
- Every sample must be reachable from navigation, reflected in the URL, and include a source code link.
- Initial sample concepts are rotating cube, lighting, texture, controls, and postprocessing.
- Prefer SOLID-friendly boundaries: app shell, routing, layout, shared runtime, Three.js helpers, and independent sample modules.

## Coding Rules

- Use strict TypeScript and avoid `any`, unchecked non-null assertions, and hidden global mutable state.
- Add TSDoc for exported types, exported functions, exported classes, sample lifecycle functions, and non-obvious public contracts.
- Use Three.js resource disposal deliberately: dispose geometries, materials, textures, controls, render targets, and postprocessing resources created by a sample.
- Keep animation loops centralized through the sample runtime. Samples expose lifecycle behavior instead of owning app-level routing or layout.
- Prefer `import type` for type-only imports and keep imports organized with Biome.
- Use `pnpm` for all package and script commands.

## UI Rules

- Build the usable sample browser as the first screen, not a landing page.
- The sidebar must support desktop collapse and mobile overlay behavior.
- Main content should prioritize the live Three.js canvas and compact sample metadata/source links.
- Keep layouts responsive, accessible, and stable; avoid text overlap and layout shifts.

## Validation

- Run `pnpm typecheck` and `pnpm check` after code changes.
- Run `pnpm build` for changes that affect app startup, routing, sample loading, or build configuration.
- For visual or interactive changes, start `pnpm dev` and verify the browser renders a nonblank scene with usable navigation.