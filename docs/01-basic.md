# Step 1 — 基本通知

最小構成のローカル通知を表示します。**通知 API 全体の土台** となる 3 要素を学びます。

## 学ぶこと

1. **NotificationChannel** の登録 (Android 8.0+ 必須)
2. **POST_NOTIFICATIONS** ランタイム権限 (Android 13+ 必須)
3. **NotificationCompat.Builder** で通知を構築・発行

## 実装ポイント

### ① マニフェスト宣言

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

→ Android 13 (API 33) から **dangerous permission** に昇格。マニフェスト宣言だけでは通知は表示されません。

### ② Application 起動時にチャンネル登録

```kotlin
class LocalNotificationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannelRegistrar.registerAll(this) // 何度呼んでも安全
    }
}
```

`createNotificationChannel` は **冪等** (同じ ID で 2 回呼んでも上書きされない) なので、毎回起動時に呼ぶのが安全策です。

### ③ ランタイム権限のリクエスト

[`Permission.kt`](../app/src/main/java/com/example/localnotification/ui/common/Permission.kt) の `rememberNotificationPermissionState()` を使います。`ActivityResultContracts.RequestPermission()` を Compose の launcher として登録し、`SDK_INT >= TIRAMISU` でガード。

### ④ Notification 構築

```kotlin
NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
    .setSmallIcon(R.drawable.ic_notification)  // 必須 (無いと post 失敗)
    .setContentTitle("こんにちは")
    .setContentText("これは Step 1 の基本ローカル通知です。")
    .setAutoCancel(true)
    .build()
```

そして `NotificationManagerCompat.from(context).notify(id, notification)`。

## 落とし穴 ⚠️

| 症状 | 原因 |
|------|------|
| 通知が出ない | (1) チャンネル未作成 (2) POST_NOTIFICATIONS 未許可 (3) `setSmallIcon` 未指定 |
| Small Icon が真っ白 | アイコンは **白色シルエット** が必須。色を入れたい場合は `setColor` で背景を着色 |
| `SecurityException` | `notify` 呼び出し前に必ず `NotificationManagerCompat.areNotificationsEnabled()` で確認 |

## 関連ファイル

- [LocalNotificationApp.kt](../app/src/main/java/com/example/localnotification/LocalNotificationApp.kt)
- [NotificationChannelRegistrar.kt](../app/src/main/java/com/example/localnotification/notification/NotificationChannelRegistrar.kt)
- [NotificationBuilders.buildBasic](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt)
- [Step1Screen.kt](../app/src/main/java/com/example/localnotification/ui/step1/Step1Screen.kt)
