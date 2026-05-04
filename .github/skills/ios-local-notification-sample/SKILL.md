---
name: ios-local-notification-sample
description: 'Workflow for designing, implementing, and reviewing a learner-friendly SwiftUI sample for iOS local notifications. Use when asked to add, teach, review, or refine local notification permission, scheduling, pending request, cancellation, or foreground presentation behavior in this repository.'
argument-hint: '[task: plan, implement, review, or debug local notifications]'
---

# iOS Local Notification Sample Skill

Use this skill when working on the local notification learning sample in this repository.

## When To Use This Skill

- The user asks to implement the local notification sample.
- The user asks to explain, review, or improve notification permission flow.
- The user asks to schedule, list, cancel, or debug local notifications.
- The user asks to make the app more useful for learners.

## Core Concepts To Preserve

- Local notifications are scheduled by the app and delivered by the system at a time, calendar condition, or location condition.
- The basic scheduling flow is content, trigger, request, then `UNUserNotificationCenter.add`.
- Notification authorization is user-controlled and can change after the first prompt.
- Foreground delivery is different from background delivery and requires a notification center delegate if the app wants to present banners, sounds, or custom handling while open.

## Workflow

1. Inspect the current app structure and confirm whether the project is still the default SwiftUI starter.
2. Define the learner outcome before coding. Example: "A learner can request permission, schedule a notification for 5 seconds later, see pending requests, and cancel them."
3. Decide the smallest complete implementation slice. Prefer permission, schedule, pending list, cancel, and optional foreground presentation before advanced notification categories.
4. Create or update Swift files under `ios-local-notification/`.
5. Keep view code declarative. Put side effects in a notification manager/service.
6. Build when full Xcode is active. If only Command Line Tools are active, report the blocker and perform static review.
7. Provide manual verification steps for simulator or device.

## Recommended Implementation Shape

- `NotificationManager` or similar: owns authorization status, latest message, pending requests, and scheduling/cancel APIs.
- `NotificationDelegate` or similar: retained object implementing `UNUserNotificationCenterDelegate` when foreground presentation is part of the lesson.
- `ContentView`: presents status, buttons, pending request list, and latest result.
- `ios_local_notificationApp`: wires any app-wide retained delegate or shared manager needed at startup.

## API Guidance

- Request permission with `UNUserNotificationCenter.current().requestAuthorization(options:)` from an explicit user action.
- Read settings with `UNUserNotificationCenter.current().notificationSettings()` before scheduling.
- Create content with `UNMutableNotificationContent` and set title, body, and sound when relevant.
- For the first demo, use `UNTimeIntervalNotificationTrigger(timeInterval:repeats:)` with a short one-shot interval.
- Create requests with `UNNotificationRequest(identifier:content:trigger:)`.
- Add requests with `try await UNUserNotificationCenter.current().add(request)`.
- Read pending requests with `await UNUserNotificationCenter.current().pendingNotificationRequests()`.
- Cancel with `removePendingNotificationRequests(withIdentifiers:)` or `removeAllPendingNotificationRequests()`.

## Gotchas

- The system shows the authorization prompt only once per app install. Delete the app or reset simulator content to test the initial prompt again.
- A notification delivered while the app is foregrounded does not automatically look the same as a background notification. Use a retained delegate for foreground presentation.
- SwiftUI previews cannot validate notification permissions or delivery.
- Do not assign `UNUserNotificationCenter.current().delegate` to a temporary object. Retain the delegate for the app lifetime.
- Avoid repeating sub-minute time interval triggers. Use a one-shot short interval for beginner demos.
- Do not add remote notification registration or push entitlements for a local notification sample.

## Troubleshooting

| Issue | Likely Cause | Action |
| --- | --- | --- |
| Permission prompt does not appear | The user already answered once for this install | Delete the app from simulator/device and run again |
| Scheduled notification is not visible | Permission is denied or the app is foregrounded without presentation options | Check settings and test background delivery or foreground delegate |
| Build cannot run with `xcodebuild` | Active developer directory is Command Line Tools | Select full Xcode before command-line validation |
| Pending list is stale | UI state mirrors old data | Refresh from `pendingNotificationRequests()` after each add/cancel |
| Cancel button appears to do nothing | Request identifiers are unstable or not retained in UI | Use stable identifiers for cancellable examples |

## References

- Apple: Scheduling a notification locally from your app: https://developer.apple.com/documentation/usernotifications/scheduling-a-notification-locally-from-your-app
- Apple: Asking permission to use notifications: https://developer.apple.com/documentation/usernotifications/asking-permission-to-use-notifications
- Apple: Handling notifications and notification-related actions: https://developer.apple.com/documentation/usernotifications/handling-notifications-and-notification-related-actions
- VS Code: Agent Skills: https://code.visualstudio.com/docs/copilot/customization/agent-skills
- VS Code: Prompt files: https://code.visualstudio.com/docs/copilot/customization/prompt-files