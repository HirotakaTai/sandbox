---
description: 'Use when optimizing Vite, DOM, CSS, Three.js rendering, canvas layout, asset loading, responsiveness, or Core Web Vitals for the sample browser.'
applyTo: 'src/**/*.ts, src/**/*.css, index.html'
---

# Web And Three.js Performance

These instructions adapt the awesome-copilot `performance-optimization.instructions.md` guidance for this Vite and Three.js learning project.

## Priorities

- Keep the first screen usable quickly: sample navigation, metadata, and the selected canvas should not depend on avoidable long tasks.
- Keep layout stable. Reserve dimensions for the sidebar, main panel, canvas container, and metadata/source-link areas.
- Keep interaction latency low. Sidebar toggles, sample switching, and controls should not trigger expensive layout work.
- Keep examples readable while still modeling good rendering and cleanup habits.

## Loading

- Prefer static imports for core app code and deliberate dynamic imports only when samples become large enough to justify it.
- Avoid render-blocking third-party scripts. This project should not need external runtime scripts for samples.
- Give fixed `width`, `height`, or stable aspect-ratio constraints to images, canvases, and fixed-format UI elements.
- Keep asset formats web-friendly and sample-specific assets close to the sample that owns them.

## Three.js Runtime

- Keep a single app-level animation loop and avoid per-sample `requestAnimationFrame` loops.
- Pause or dispose inactive sample resources during sample switches.
- Avoid allocating geometries, materials, vectors, colors, or arrays inside hot `update` paths unless the allocation is intentional and bounded.
- Use renderer sizing based on the canvas container, and update camera aspect/projection only when the container size changes.
- Tune renderer pixel ratio with an upper bound so high-DPI screens do not create excessive GPU load.

## DOM And CSS

- Prefer class changes and CSS transitions for sidebar state over repeated inline style mutation.
- Avoid forced synchronous layout patterns such as reading layout immediately after writing styles in the same frame.
- Keep the DOM shallow and predictable; this is a tool UI, not a marketing page.
- Do not let hover, active, loading, or source-link states resize surrounding layout.

## Validation

- For visual or interactive changes, verify the scene is nonblank after initial load and after switching samples.
- Run `pnpm build` for changes to startup, routing, runtime, imports, or asset handling.
- Treat repeated frame drops, rising memory after sample switching, or blank canvas output as implementation issues, not visual polish tasks.