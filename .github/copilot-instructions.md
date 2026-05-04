# Copilot Repository Instructions

This repository is a newly created SwiftUI iOS app intended to become a learner-friendly sample for iOS local notifications.

## Project Layout

- `ios-local-notification/ContentView.swift` contains the current SwiftUI starter view.
- `ios-local-notification/ios_local_notificationApp.swift` contains the SwiftUI `@main` app entry point.
- `ios-local-notification/Assets.xcassets/` contains the generated accent color and app icon asset catalogs.
- `ios-local-notification.xcodeproj/project.pbxproj` is an Xcode 26-era project using a file system synchronized root group for `ios-local-notification/`. New Swift source files placed under that app folder should be picked up by Xcode without hand-editing the project file.

## Current Build Facts

- App target and scheme name: `ios-local-notification`.
- Product bundle identifier: `com.example.ios-local-notification`.
- Deployment target in the project file: iOS `26.2`.
- Swift language setting: `SWIFT_VERSION = 5.0`.
- The project enables `SWIFT_DEFAULT_ACTOR_ISOLATION = MainActor` and `SWIFT_APPROACHABLE_CONCURRENCY = YES`.
- The app uses generated Info.plist settings (`GENERATE_INFOPLIST_FILE = YES`).

## Implementation Guidance

- Keep the sample focused on local notifications only. Do not add push notification entitlements, a server, or third-party dependencies unless explicitly requested.
- Prefer small, learnable Swift files over dense single-file demos. A notification manager/service plus a compact SwiftUI view is a good default.
- Use `UserNotifications` APIs directly: `UNUserNotificationCenter`, `UNMutableNotificationContent`, `UNNotificationRequest`, and appropriate trigger types.
- Request notification authorization from an explicit user action and explain the outcome in the UI. Do not request permission automatically on first launch.
- Check `UNUserNotificationCenter.current().notificationSettings()` before scheduling notifications because users can change authorization in Settings.
- Use stable request identifiers when the UI needs to cancel or replace scheduled requests. Use generated identifiers only for fire-and-forget examples.
- If demonstrating foreground notification presentation, set and retain a `UNUserNotificationCenterDelegate` early in app startup. A temporary delegate object will be deallocated.
- Keep learner-facing UI copy concise and practical. The app should open directly into the usable sample, not a landing page.

## Validation

- With full Xcode selected, validate with a command like `xcodebuild -project ios-local-notification.xcodeproj -scheme ios-local-notification -destination 'platform=iOS Simulator,name=<available iPhone simulator>' build`.
- If `xcodebuild` reports that the active developer directory is `/Library/Developer/CommandLineTools`, select a full Xcode installation before relying on command-line builds.
- Xcode previews cannot validate notification prompts or delivery. Run the app on a simulator or device for notification behavior.
- After changing notification behavior, manually verify permission request, permission denied state, scheduling, pending request listing, cancellation, and foreground delivery behavior when applicable.

## References

- GitHub Copilot repository instructions: https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions
- VS Code custom instructions: https://code.visualstudio.com/docs/copilot/customization/custom-instructions
- Apple local notification scheduling: https://developer.apple.com/documentation/usernotifications/scheduling-a-notification-locally-from-your-app
- Apple notification permission guidance: https://developer.apple.com/documentation/usernotifications/asking-permission-to-use-notifications