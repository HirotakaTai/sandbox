# Android Local Notification 学習サンプル

> **対象バージョン**: 2026 年 4 月時点で安定している構成
> AGP 8.7.3 / Kotlin 2.0.21 / Compose BOM 2024.12.01 / Material 3 / Navigation Compose / WorkManager
> **minSdk** 24 / **targetSdk** 36

Android のローカル通知 (Local Notification) を **段階的に** 学べる Jetpack Compose 製サンプルアプリです。各 Step は独立した画面として実装してあり、順番に進めることで通知 API のベストプラクティスを体系的に習得できます。

## 学べること (7 Step 構成)

| Step | 画面 | 主に学ぶ API / 概念 |
|------|------|--------------------|
| 1 | [基本通知](docs/01-basic.md) | `NotificationChannel` / `NotificationCompat.Builder` / POST_NOTIFICATIONS ランタイム権限 |
| 2 | [タップで画面遷移](docs/02-tap-to-open.md) | `PendingIntent.getActivity` / `FLAG_IMMUTABLE` / Trampoline 禁止 / launchMode |
| 3 | [スタイル付き通知](docs/03-styles.md) | `BigTextStyle` / `BigPictureStyle` / `InboxStyle` |
| 4 | [アクション + 返信](docs/04-actions-reply.md) | `addAction` / `RemoteInput` / `BroadcastReceiver` での完結 |
| 5 | [進捗通知](docs/05-progress.md) | `setProgress` / `setOngoing` / `setOnlyAlertOnce` |
| 6 | [グループ通知](docs/06-grouped.md) | `setGroup` / `setGroupSummary` / Bundle 表示 |
| 7 | [予約通知 (WorkManager)](docs/07-scheduled.md) | `OneTimeWorkRequest` + `setInitialDelay` / Doze と inexact |

各 Step は単独で動かせます。ホーム画面のカードから移動してください。

## アーキテクチャ概要

シンプルな **単一 Activity + Compose Navigation** 構成です。学習目的なので Hilt / Room / Retrofit などの追加ライブラリは敢えて入れていません。

```
                              ┌─────────────────────────┐
                              │      MainActivity       │
                              │  (ComponentActivity)    │
                              │   setContent { ... }    │
                              └────────────┬────────────┘
                                           │
                              ┌────────────▼────────────┐
                              │   AppNavHost (Compose)  │
                              │  Routes: Home, Step1〜7 │
                              └────────────┬────────────┘
                                           │ navigate
        ┌──────────────────────────────────┼──────────────────────────────────┐
        ▼                                  ▼                                  ▼
 ┌─────────────┐                ┌─────────────────────┐               ┌─────────────────┐
 │ HomeScreen  │                │   StepN Screens     │               │   共通 UI       │
 │  Step List  │                │   (1, 2, 3, ... 7)  │◀──────────────│ PermissionState │
 └─────────────┘                └──────────┬──────────┘               │ StepScaffold    │
                                           │                          └─────────────────┘
                                           │ uses
                                           ▼
                              ┌─────────────────────────┐
                              │    notification/         │  ← この層が「通知ドメイン」
                              │   - NotificationIds     │
                              │   - ChannelRegistrar    │
                              │   - NotificationPoster  │
                              │   - NotificationBuilders│
                              │   - ActionReceiver      │  ← Step 4 (Trampoline 禁止対応)
                              │   - NotificationEvents  │  ← Receiver→UI イベントバス
                              │   - WorkScheduler       │  ← Step 7 (WorkManager ラッパー)
                              │   - NotificationWorker  │
                              └─────────────────────────┘
                                           │
                                           ▼
                              ┌─────────────────────────┐
                              │  NotificationManager    │
                              │     Compat (OS API)     │
                              └─────────────────────────┘
```

### レイヤー分け

| 層 | パッケージ | 役割 |
|----|------------|------|
| Application | `com.example.localnotification` | `Application` 起動時に通知チャンネル登録 |
| Domain | `notification/` | 通知の「組み立て」「発行」「アクション処理」「予約」を担う純粋な層 |
| UI | `ui/` | Compose 画面、Navigation、権限ハンドラ |

### なぜ ViewModel を作っていないか

各 Step 画面はステートが小さい (進捗 1 値、最後のイベント 1 件など) ため、`remember { mutableStateOf(...) }` で完結します。学習に集中させるための判断です。本格アプリでは `ViewModel` + `StateFlow` に切り出してください (`androidx.lifecycle:lifecycle-viewmodel-compose` は依存に入れてあります)。

## セットアップ

### 必要環境
- **Android Studio** Ladybug | 2024.2.1 以上
- **JDK** 17+ (Compose Compiler 2.x 要件)
- **Android SDK** Platform 36

### 実行手順
```bash
./gradlew :app:installDebug
```
または Android Studio で `app` を **Run** してください。Android 13+ 端末で初回起動すると、各 Step ボタン押下時に **POST_NOTIFICATIONS** の許可ダイアログが表示されます。

> **権限を 2 回拒否した場合**: OS が以後ダイアログを表示しなくなります。各 Step の上部に表示される赤いカードから「通知設定を開く」を押し、設定アプリで手動有効化してください。

## ベストプラクティスの要点 (このリポジトリで守っていること)

1. ✅ **POST_NOTIFICATIONS は必ずランタイムで取得** (Manifest 宣言だけでは Android 13+ で発行されない)
2. ✅ **NotificationChannel は Application 起動時に一度だけ登録** (再登録しても OS が無視するので副作用なし)
3. ✅ **PendingIntent には `FLAG_IMMUTABLE` を必須付与** (Android 12+ クラッシュ回避)。RemoteInput の返信時のみ `FLAG_MUTABLE`
4. ✅ **Notification Trampoline (Receiver/Service→Activity) を使わない** (Android 12+ で黙って失敗)。タップで画面を開く場合は `PendingIntent.getActivity` を使う
5. ✅ **進捗通知は `setOnlyAlertOnce(true)`** で更新時に音/バイブを鳴らさない
6. ✅ **グループ通知は子 → サマリーの順で発行** (順序により表示が変わるバグ回避)
7. ✅ **予約通知は WorkManager で行う** (再起動耐性 + Doze 協調)。`SCHEDULE_EXACT_ALARM` は使わない (Android 14+ ユーザー確認が必要)

詳細は [docs/architecture.md](docs/architecture.md) と各 Step の解説を参照してください。

## 関連ドキュメント

- [docs/architecture.md](docs/architecture.md) — レイヤー、依存方向、ファイル構成の詳細
- [docs/00-permissions-channels.md](docs/00-permissions-channels.md) — 全 Step 共通の前提 (権限とチャンネル)
- [docs/01-basic.md](docs/01-basic.md) ... [docs/07-scheduled.md](docs/07-scheduled.md) — 各 Step の深掘り解説
- [.github/instructions/android-local-notification.instructions.md](.github/instructions/android-local-notification.instructions.md) — このリポジトリで AI エージェントが守っているルール集

## ライセンス

学習用サンプル。自由に利用・改変可。
