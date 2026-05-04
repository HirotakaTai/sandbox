---
description: 'Plan and implement the learner-friendly iOS local notification sample for this SwiftUI app'
name: 'Implement Local Notification Sample'
agent: 'ios-local-notification-teacher'
argument-hint: '[sample scope or learner goal]'
---

# Implement Local Notification Sample

Use this prompt to turn the starter SwiftUI app into a learner-friendly local notification sample.

## Inputs

- Sample scope: `${input:sampleScope:basic permission, schedule, pending list, cancel, and foreground behavior}`
- Learner level: `${input:learnerLevel:beginner iOS developer}`

## Required Context

Read these files before editing:

- [Repository instructions](../copilot-instructions.md)
- [Swift local notification instructions](../instructions/ios-local-notification-sample.instructions.md)
- [Local notification sample skill](../skills/ios-local-notification-sample/SKILL.md)
- [Current SwiftUI view](../../ios-local-notification/ContentView.swift)
- [App entry point](../../ios-local-notification/ios_local_notificationApp.swift)

## Workflow

1. Summarize the current project state in a few bullets.
2. Define learner outcomes and acceptance criteria for the requested sample scope.
3. Identify the Swift files that need to be created or edited.
4. Implement the sample with focused changes only.
5. Validate build behavior when the environment has full Xcode selected.
6. Provide manual simulator/device verification steps for permission prompt, scheduling, delivery, foreground behavior, pending request listing, and cancellation.

## Implementation Requirements

- Use SwiftUI and `UserNotifications` only.
- Do not add push notification entitlements, remote notification registration, servers, or third-party dependencies.
- Do not request notification permission on launch. Use an explicit UI action.
- Keep notification side effects in a manager/service rather than inside view rendering code.
- Prefer adding Swift files under `ios-local-notification/` without editing the Xcode project file.
- Make denied authorization and errors visible in the UI.

## Output Expectations

- Apply code changes directly.
- Keep the final response concise: changed files, validation result, and remaining manual checks.
- If validation cannot run because the environment only has Command Line Tools selected, state that exact blocker and continue with source-level checks.