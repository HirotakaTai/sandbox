---
applyTo: '**/*.kt,**/AndroidManifest.xml'
description: 'Android (API 33+) におけるローカル通知実装の必須ルール。POST_NOTIFICATIONS 権限、Notification Channel、PendingIntent、trampoline 制限を網羅。'
---

# Android Local Notification — 実装ルール

このプロジェクトのコア学習領域です。**ここに書かれたルールから逸脱した実装を提案してはいけません。**
迷ったら必ず公式ドキュメント (`developer.android.com/develop/ui/views/notifications`) を再確認してください。

## 必須権限

### `POST_NOTIFICATIONS` (Android 13 / API 33+)

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

- **dangerous permission** なので **ランタイムリクエスト必須**。
- リクエストは `ActivityResultContracts.RequestPermission` を使う。
- API 32 以下ではマニフェスト宣言だけで自動許可されるため、`ContextCompat.checkSelfPermission` は API 33+ でのみ意味を持つ。

```kotlin
// Compose の場合
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
) { granted ->
    if (granted) postNotification() else showRationale()
}

LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
    ) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
```

- **shouldShowRequestPermissionRationale が true** のときは目的説明 UI を必ず表示する (UX 必須、Play Store ポリシー)。
- 2 回拒否されると OS が自動的に「拒否」を返すため、**設定画面への導線**を準備する。

### 任意のスケジュール用権限 (使う場合のみ)

| 用途 | 権限 | 必要バージョン |
|------|------|----------------|
| ユーザー起点でない正確なアラーム (リマインダー等) | `SCHEDULE_EXACT_ALARM` | API 31+ (ユーザー許可必要、API 34 から取得困難) |
| カレンダー / アラームクロック等の本質的アプリ | `USE_EXACT_ALARM` | API 33+ (Play Store 審査あり) |
| 起動時にアラームを再登録 | `RECEIVE_BOOT_COMPLETED` | 不要に取らない |

→ **本サンプルは原則 inexact alarm (`AlarmManager.set` / `setWindow`) または `WorkManager` を使用**し、exact alarm は学習用に明示する場合のみ。

## Notification Channel (API 26+ 必須)

```kotlin
object NotificationChannels {
    const val LEARNING = "learning_channel"
}

fun ensureChannels(context: Context) {
    // NotificationManagerCompat.createNotificationChannel は API 26+ で実体化される
    val channel = NotificationChannelCompat.Builder(
        NotificationChannels.LEARNING,
        NotificationManagerCompat.IMPORTANCE_DEFAULT, // = NotificationManager.IMPORTANCE_DEFAULT
    )
        .setName(context.getString(R.string.channel_learning_name))
        .setDescription(context.getString(R.string.channel_learning_desc))
        .build()
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}
```

- **Application#onCreate で 1 度だけ呼ぶ** (重複作成は no-op だが性能のため)。
- ID と名称・説明を `strings.xml` に置き i18n 対応する。
- IMPORTANCE 一覧: `MIN`(1) / `LOW`(2) / `DEFAULT`(3) / `HIGH`(4)。HIGH 以上はヘッドアップ通知になるので濫用禁止。
- ユーザーが OS 設定で変更したチャンネル属性 (sound, importance) はアプリ側から変更できない。再作成も無効。

## NotificationCompat.Builder

```kotlin
fun buildBasicNotification(context: Context, contentIntent: PendingIntent): Notification {
    return NotificationCompat.Builder(context, NotificationChannels.LEARNING)
        .setSmallIcon(R.drawable.ic_notification) // 必須。モノクロベクターを使用
        .setContentTitle(context.getString(R.string.notif_title))
        .setContentText(context.getString(R.string.notif_text))
        .setContentIntent(contentIntent)
        .setAutoCancel(true) // タップで自動削除
        .setPriority(NotificationCompat.PRIORITY_DEFAULT) // API 25 以下のための保険
        .build()
}
```

| 項目 | 注意点 |
|------|--------|
| `setSmallIcon` | **必須**。アルファチャンネルのみ有効 (色は無視)。`@drawable/ic_notification` をモノクロで用意。 |
| 大きい本文 | `BigTextStyle` (1 行超のテキスト) / `BigPictureStyle` (画像) / `InboxStyle` (リスト) を使い分ける。 |
| アクション | `addAction(icon, title, pendingIntent)` で最大 3 つ。Reply には `RemoteInput` を併用。 |
| 進捗 | `setProgress(max, progress, indeterminate)`。完了時は `setProgress(0,0,false)` + 別通知更新。 |
| グループ | `setGroup("key")` + サマリー通知 (`setGroupSummary(true)`) を Android 7+ で表示。 |

## 通知の発行

```kotlin
private const val NOTIFICATION_ID = 1001

fun show(context: Context, notification: Notification) {
    val nm = NotificationManagerCompat.from(context)
    // API 33+ で権限がない場合は notify() が黙って失敗する。事前に必ずチェック。
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
    ) {
        return // 権限要求はこのメソッドの責務外
    }
    nm.notify(NOTIFICATION_ID, notification)
}
```

- 通知 ID はアプリ内ユニーク。同 ID で `notify` すると上書き更新。
- タグ + ID で名前空間を分けられる: `nm.notify("chat:42", 0, notification)`。

## PendingIntent の正しい作り方

```kotlin
val intent = Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
}
val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
val pi = PendingIntent.getActivity(context, /* requestCode = */ 0, intent, flags)
```

- **Android 12 (API 31) 以降は `FLAG_IMMUTABLE` または `FLAG_MUTABLE` を必ず指定** (省略するとクラッシュ)。
- mutable が必要なのは `RemoteInput` を使う Reply 系のみ。それ以外は IMMUTABLE。
- requestCode を ID 別に分けないと Extras が前回のものと共有される (再利用のバグ源)。

## Notification Trampoline 禁止 (Android 12+)

通知タップから `BroadcastReceiver` / `Service` 経由で Activity を起動することは **禁止** (黙って起動失敗する)。

- 通知 → 直接 Activity (`PendingIntent.getActivity`) が原則。
- バックグラウンド処理が必要なら `Activity` から起動された後で WorkManager 等を使う。

## Foreground Service (関連トピック)

通知と密接なため記載:
- Android 14 (API 34) 以降は `<service android:foregroundServiceType="..."/>` が**必須**。
- `Service.startForeground(id, notification, type)` で `type` を渡す。
- 未指定で起動すると `MissingForegroundServiceTypeException` で即クラッシュ。
- `dataSync`, `mediaPlayback`, `location`, `phoneCall` 等から正しい type を選ぶ。
- 本サンプルでは原則 foreground service は使わない。学習でやる場合は別 instructions に従う。

## デバッグ Tips

- 通知が出ない時に確認すること:
  1. `Settings → Apps → <app> → Notifications` でチャンネルが ON か
  2. `adb shell dumpsys notification --noredact` で Posted Notifications を確認
  3. `Build.VERSION.SDK_INT` ガードと `POST_NOTIFICATIONS` 権限
  4. `setSmallIcon` を設定しているか (これが無いと post 失敗)
  5. PendingIntent の flag に IMMUTABLE/MUTABLE があるか
- ログタグは `Log.d("Notif", ...)` 等で統一する。

## チェックリスト (新規通知機能を提案する前に必ず確認)

- [ ] `POST_NOTIFICATIONS` をランタイムリクエストする実装がある
- [ ] `NotificationChannel` が Application#onCreate で作成される
- [ ] `setSmallIcon` がモノクロベクターで指定されている
- [ ] `PendingIntent` に `FLAG_IMMUTABLE` (または `FLAG_MUTABLE`) を指定している
- [ ] Trampoline (Receiver/Service 経由) で Activity を起動していない
- [ ] 文字列はすべて `strings.xml`
- [ ] importance / priority が用途に対して妥当 (デフォルトは DEFAULT)
- [ ] テスト可能な薄いラッパー越しに `NotificationManagerCompat` を呼んでいる
