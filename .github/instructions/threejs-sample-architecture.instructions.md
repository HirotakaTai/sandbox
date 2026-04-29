---
description: 'Use when writing Three.js sample runtime, sample modules, routing, metadata, source links, documentation, or disposal logic.'
applyTo: 'src/**/*.ts, docs/samples/**/*.md'
---

# Three.js Sample Architecture

Use these rules for TypeScript modules and docs that define or support samples.

## Sample Contract

- Model each sample as an independent module with stable metadata and lifecycle behavior.
- Use kebab-case sample IDs. Treat IDs as URL contracts after they are introduced.
- Include metadata for display name, description, tags, difficulty, and source code path.
- Support lifecycle functions equivalent to `init`, `update`, and `dispose`; keep routing, sidebar DOM, and app shell concerns outside sample modules.
- Keep source code links mandatory. A sample without a source path is incomplete.

## Runtime Boundaries

- Keep the animation loop, canvas ownership, resize handling, URL synchronization, and active sample switching in app/runtime code.
- Samples may create scenes, cameras, meshes, materials, textures, controls, loaders, and passes, but they must expose cleanup through the lifecycle contract.
- Use `deltaTime` or elapsed time supplied by the runtime for animation. Avoid each sample creating its own unrelated `requestAnimationFrame` loop.
- Avoid module-level mutable Three.js instances except constants that are immutable and safely shared.

## Three.js Resource Management

- Dispose resources created by the sample: geometries, materials, textures, render targets, controls, postprocessing composers, and event listeners.
- When a material can be an array, handle both single material and material array disposal paths.
- Remove DOM or window listeners in `dispose` if a sample registers them.
- Keep renderer-level resources in shared runtime unless a sample explicitly needs its own offscreen target.

## TypeScript Standards

- Define explicit interfaces for metadata, lifecycle context, and disposable resources.
- Prefer `unknown` plus narrowing over `any`.
- Avoid non-null assertions. Use guards and clear error paths when DOM elements or sample IDs are missing.
- Use `import type` for types imported from `three` or local modules.
- Add TSDoc to exported contracts and lifecycle entry points.

## Documentation

- For each sample, document the learning goal, visible behavior, relevant Three.js APIs, and source file path.
- Keep documentation concise and tied to the actual implementation.