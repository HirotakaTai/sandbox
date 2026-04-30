# Step 4 — アクション + RemoteInput (返信)

通知に「既読」ボタンと **インライン返信** を追加します。Trampoline 禁止と最も向き合う Step です。

## 学ぶこと

1. **`NotificationCompat.Action`** で通知ボタンを追加
2. **`RemoteInput`** で返信入力欄を inline 表示
3. **BroadcastReceiver で完結** (Activity を起動しない)
4. **FLAG_MUTABLE** が必要な唯一のケース

## 実装ポイント

### 既読ボタン (シンプルなアクション)

```kotlin
val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
    action = ACTION_MARK_READ
    putExtra(EXTRA_NOTIF_ID, notifId)
}
val markReadPi = PendingIntent.getBroadcast(
    context, 4001, markReadIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)
```

→ `addAction(icon, label, markReadPi)` で追加。

### 返信ボタン (RemoteInput)

```kotlin
val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
    .setLabel("返信を入力")
    .build()

val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
    action = ACTION_REPLY
    putExtra(EXTRA_NOTIF_ID, notifId)
}
// ★ FLAG_MUTABLE が必須: RemoteInput は OS が値を Intent に注入するため
val replyPi = PendingIntent.getBroadcast(
    context, 4002, replyIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
)
val replyAction = NotificationCompat.Action.Builder(icon, "返信", replyPi)
    .addRemoteInput(remoteInput)
    .setAllowGeneratedReplies(true)
    .build()
```

### Receiver 側

```kotlin
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_READ -> { /* 通知を消す */ }
            ACTION_REPLY -> {
                val text = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_TEXT_REPLY)?.toString().orEmpty()
                // 返信を保存し、通知を更新する
            }
        }
    }
}
```

> Receiver は **数秒以内に完了** する必要があります (ANR 回避)。重い I/O が必要な場合は `goAsync()` か WorkManager に委譲。

### Receiver → UI のイベント伝達

`NotificationEvents` という SharedFlow を使い、Receiver が emit、UI 側が `LaunchedEffect` で collect します。プロセス内 singleton で十分です。

## 落とし穴 ⚠️

| 症状 | 対策 |
|------|------|
| 返信ボタンを押しても何も起きない | `FLAG_MUTABLE` を忘れている。または `addRemoteInput` 呼び忘れ |
| アクションを押すとアプリが落ちる | Receiver を Manifest に登録していない or `exported` 未指定 (Android 13+) |
| 返信テキストが取得できない | `getResultsFromIntent` の引数は **元の intent**。Receiver 引数の `intent` をそのまま渡す |

## 関連ファイル

- [NotificationBuilders.buildWithActions](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt)
- [NotificationActionReceiver.kt](../app/src/main/java/com/example/localnotification/notification/NotificationActionReceiver.kt)
- [NotificationEvents.kt](../app/src/main/java/com/example/localnotification/notification/NotificationEvents.kt)
- [Step4Screen.kt](../app/src/main/java/com/example/localnotification/ui/step4/Step4Screen.kt)
