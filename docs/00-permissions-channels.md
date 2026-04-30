# Step 0 — 共通の前提: 権限とチャンネル

各 Step の前に **必ず** 理解しておくべき 2 つの仕組みです。

## 1. POST_NOTIFICATIONS ランタイム権限 (Android 13+)

### マニフェスト宣言

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

これだけでは Android 13 (API 33) 以降では通知が表示されません。**dangerous permission** に昇格したため、ユーザーから明示的な許可が必要です。

### ランタイムリクエスト

`ActivityResultContracts.RequestPermission()` を使います。Compose では `rememberLauncherForActivityResult` で wrap して使うのが定石。

```kotlin
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
) { granted -> /* 結果を state に反映 */ }

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
}
```

API 32 以下では **マニフェスト宣言のみで自動許可** されるため、launch を呼ぶ必要はありません (呼んでも no-op)。

### 拒否されたら

ユーザーが 2 回拒否すると、OS がそれ以上ダイアログを表示しなくなります (Android 11+ の挙動)。本サンプルでは [`PermissionStatusCard`](../app/src/main/java/com/example/localnotification/ui/common/Permission.kt) が "通知設定を開く" ボタンを表示し、設定アプリへ導線を提供しています:

```kotlin
val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
    data = Uri.fromParts("package", context.packageName, null)
}
context.startActivity(intent)
```

## 2. NotificationChannel (Android 8.0+ 必須)

### 役割

チャンネルは **ユーザーが通知の種類ごとに音/バイブ/優先度をカスタマイズできる単位**。アプリは「種類」を宣言するだけ、設定は OS とユーザーに委ねるという設計思想です。

### 登録は Application 起動時

```kotlin
class LocalNotificationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannelRegistrar.registerAll(this)
    }
}
```

`createNotificationChannel` は **冪等** なので毎回呼んで OK。**ただし以下のフィールドは初回のみ反映** されます:
- `importance` (優先度)
- `name`
- `description`
- 音、バイブパターン、ライト色

→ ユーザーが個別に変更した設定があれば **常にそちらが優先** されます。

### チャンネル ID は不変

公開後にチャンネル ID を変更すると、ユーザーがカスタマイズした設定が引き継がれません。挙動を変えたい場合は:

1. 新しい ID で新規チャンネルを作成
2. 旧チャンネルを `notificationManager.deleteNotificationChannel(oldId)`

本サンプルのチャンネル定義: [NotificationChannelRegistrar.kt](../app/src/main/java/com/example/localnotification/notification/NotificationChannelRegistrar.kt)

| ID | importance | 用途 |
|----|-----------|------|
| `basic` | DEFAULT | Step 1, 2, 3, 5 |
| `actions` | HIGH | Step 4 (heads-up したい返信通知) |
| `grouped` | DEFAULT | Step 6 |
| `scheduled` | DEFAULT | Step 7 |

### importance と priority

| Channel importance | 振る舞い |
|--------------------|---------|
| HIGH | 画面上にポップアップ (heads-up) + 音 |
| DEFAULT | 通知バー + 音 |
| LOW | 通知バーのみ (音なし) |
| MIN | 通知バーに最小表示のみ |

`NotificationCompat.Builder.setPriority()` は **API 25 以下のフォールバック** です。API 26+ では完全にチャンネル設定が優先されます。

## 関連ファイル

- [AndroidManifest.xml](../app/src/main/AndroidManifest.xml)
- [LocalNotificationApp.kt](../app/src/main/java/com/example/localnotification/LocalNotificationApp.kt)
- [NotificationChannelRegistrar.kt](../app/src/main/java/com/example/localnotification/notification/NotificationChannelRegistrar.kt)
- [Permission.kt](../app/src/main/java/com/example/localnotification/ui/common/Permission.kt)
