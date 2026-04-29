---
description: 'Use when implementing the sample browser UI, sidebar navigation, responsive layout, DOM interactions, CSS, accessibility, or visual verification.'
applyTo: 'src/**/*.ts, src/**/*.css, index.html'
---

# Sample Browser UI

Use these rules when building the visible application shell.

## Layout

- Show the working sample browser immediately. Do not create a marketing-style landing page.
- Use a left sidebar for the sample list and a right/main area for the active Three.js sample.
- Keep the sidebar fixed-width on desktop, collapsible with animation, and usable as an overlay on mobile.
- Keep the main panel stable while samples switch; avoid controls or labels resizing the canvas unexpectedly.

## Navigation

- Reflect the selected sample in the URL so direct links reopen the same sample.
- Highlight the active sample in the sidebar and keep keyboard navigation usable.
- Provide an obvious source code link for the active sample.
- Handle unknown sample IDs by falling back to a known sample and updating UI state consistently.

## Accessibility

- Use semantic buttons for interactive controls, not clickable `div` elements.
- Add `aria-expanded`, `aria-controls`, and clear labels for sidebar open/close controls.
- Keep focus visible and do not trap focus unless a mobile overlay is open.
- Ensure text has sufficient contrast and does not overlap at mobile widths.

## Visual Quality

- Prefer a quiet, tool-like interface that keeps attention on the rendered scene.
- Avoid decorative cards inside cards, oversized hero typography, and one-note color palettes.
- Use stable dimensions for canvas containers, sidebars, toolbars, and metadata areas.
- Verify that the canvas is nonblank and correctly framed after layout or runtime changes.