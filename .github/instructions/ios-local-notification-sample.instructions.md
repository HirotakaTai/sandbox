---
description: 'SwiftUI and UserNotifications conventions for the iOS local notification learning sample'
applyTo: 'ios-local-notification/**/*.swift'
---

# iOS Local Notification Sample Instructions

Use these rules when creating or editing Swift files for this local notification learning sample.

## Architecture

- Keep notification-specific code out of SwiftUI view bodies. Put permission, settings, scheduling, listing, and cancellation logic in a small manager or service type.
- Keep `ContentView` focused on rendering state and invoking clear actions.
- Place new app source files under `ios-local-notification/` so Xcode's file system synchronized group can include them automatically.
- Avoid editing `ios-local-notification.xcodeproj/project.pbxproj` unless Xcode fails to discover a required file.
- Use no third-party packages for local notification basics.

## Swift And Concurrency

- Prefer `async`/`await` APIs from `UserNotifications` where available.
- Keep UI-observable state on the main actor.
- Avoid force unwraps and forced casts in sample code. Surface failures as user-visible status messages or typed errors.
- Use descriptive names that expose the learning goal, such as `requestAuthorization`, `scheduleTimeIntervalNotification`, `pendingRequests`, and `cancelAllPendingRequests`.

## Permission Flow

- Request authorization only after the learner taps an explicit permission action.
- Request only the capabilities the sample uses, usually `.alert`, `.sound`, and optionally `.badge`.
- Read current settings with `UNUserNotificationCenter.current().notificationSettings()` before scheduling.
- Handle `.notDetermined`, `.denied`, `.authorized`, `.provisional`, and `.ephemeral` explicitly when presenting status.
- If authorization is denied, show a non-blocking state that tells the learner scheduling will not be visible until permissions change.

## Scheduling Flow

- For the primary demo, use a one-shot `UNTimeIntervalNotificationTrigger` with a short interval and `repeats: false`.
- Use `UNMutableNotificationContent` with a clear title, body, and `UNNotificationSound.default` when sound is part of the lesson.
- Register requests through `UNUserNotificationCenter.current().add(request)` and handle thrown errors.
- Provide cancellation examples with `removePendingNotificationRequests(withIdentifiers:)` or `removeAllPendingNotificationRequests()`.
- When listing scheduled notifications, read pending requests from `pendingNotificationRequests()` instead of mirroring state manually.

## Foreground Presentation

- Local notifications delivered while the app is foregrounded are routed to the notification center delegate.
- If the sample demonstrates foreground banners or sounds, create a retained delegate object that implements `UNUserNotificationCenterDelegate`.
- Set the delegate during app startup or manager initialization before scheduling demo notifications.
- Make foreground presentation behavior visible in the UI so learners understand why a delegate exists.

## Learner Experience

- Show the current permission status, scheduled request count, and latest action result.
- Provide separate controls for requesting permission, scheduling a demo notification, refreshing pending requests, and canceling pending requests.
- Keep explanations short and embedded near the relevant control. Avoid long tutorial prose inside the app.
- Prefer deterministic examples over background-task or location-trigger examples for the first sample.

## Validation

- Build with Xcode or `xcodebuild` after source edits when a full Xcode installation is selected.
- Test notification behavior on a simulator or physical device. Do not rely on SwiftUI previews for permission prompts or delivery.
- Verify both allowed and denied permission paths when possible by deleting the app from the simulator/device between runs.