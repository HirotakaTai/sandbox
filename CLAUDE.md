# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android local notification learning sample (8 progressive steps) built with Jetpack Compose. Each step is an independent screen covering: basic notifications → tap-to-open → styles → actions/reply → progress → grouped → scheduled (WorkManager) → FCM mock.

- Language: Kotlin 2.0.21, Min SDK 24, Target SDK 36
- Build System: Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`)

## Build & Run Commands

```bash
# Build and install to connected device
./gradlew :app:installDebug

# Run unit tests
./gradlew :app:testDebug

# Run instrumented tests (requires connected device/emulator)
./gradlew :app:connectedAndroidTest

# Clean
./gradlew clean
```

Setup requires Android Studio Ladybug+, JDK 17+, SDK Platform 36.

## Architecture

Single-module app with a clean layered structure:

```
UI Layer        app/src/main/java/.../ui/          (Compose screens, no ViewModels)
Domain Layer    app/src/main/java/.../notification/ (pure logic + OS wrappers)
OS APIs         NotificationManagerCompat, WorkManager
```

**Key files:**
- `LocalNotificationApp.kt` — Application subclass; registers all notification channels once at startup
- `MainActivity.kt` — Single Activity with Compose Navigation; `launchMode="singleTask"` for deep-link routing
- `notification/NotificationBuilders.kt` — Pure functions building each step's notification; designed to be unit-testable
- `notification/NotificationPoster.kt` — Wraps `NotificationManagerCompat.notify`
- `notification/NotificationEvents.kt` — `SharedFlow<Event>` bus bridging `BroadcastReceiver` (short-lived) to Compose UI
- `notification/WorkScheduler.kt` + `NotificationWorker.kt` — Step 7 scheduled notifications via WorkManager
- `notification/remote/` — Step 8 FCM mock (no real Firebase dependency)
- `ui/AppNavHost.kt` — Navigation graph wiring all step screens

**Deliberate design choices:**
- No ViewModel — step state fits in `remember { mutableStateOf(...) }` to reduce learning noise
- No Hilt — manual wiring in Application/object companions for the same reason
- `NotificationEvents` uses `SharedFlow` (not LiveData) because `BroadcastReceiver` lifespan (~10 s) is incompatible with LiveData observers
- WorkManager used for scheduling (Step 7) instead of `AlarmManager`/`SCHEDULE_EXACT_ALARM` to avoid Android 14+ user-confirmation friction
- All notification IDs centralized in `NotificationIds.kt` (1xxx range, 10-per-step allocation)

## Notification API Constraints

These rules come from `.github/instructions/android-local-notification.instructions.md` and must be followed:

- **POST_NOTIFICATIONS** (`android.permission.POST_NOTIFICATIONS`) must be requested at runtime on API 33+ via `ActivityResultContracts.RequestPermission`
- **Channels** must be registered once in `Application#onCreate`; re-registration is a no-op and preserves user settings
- **PendingIntent** must use `FLAG_IMMUTABLE` (use `FLAG_MUTABLE` only when `RemoteInput` reply is needed)
- **No trampoline** — a `BroadcastReceiver` or `Service` must not launch an `Activity`; use `PendingIntent.getActivity` directly
- **Progress notifications** must set `setOnlyAlertOnce(true)` to suppress repeated sound/vibration during updates
- **Grouped notifications** — emit child notifications before the summary notification (ordering affects display)

## Commit Convention

Use conventional commits: `feat:`, `fix:`, `docs:`, `chore:`. Japanese descriptions are acceptable.
