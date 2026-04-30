# Step 5 — 進捗通知

ダウンロード進捗のような **逐次更新する通知** の実装です。`setProgress` の使い方と更新時の注意点を学びます。

## 学ぶこと

1. `setProgress(max, current, indeterminate)` で進捗バー表示
2. `setOnlyAlertOnce(true)` で更新時の音/バイブを抑止
3. `setOngoing(true)` でユーザーがスワイプ消去できないようにする
4. 完了時は `setProgress(0, 0, false)` で進捗バーを除去 + `setAutoCancel(true)`

## 実装ポイント

```kotlin
NotificationCompat.Builder(context, channelId)
    .setSmallIcon(...)
    .setContentTitle("ファイルをダウンロード中")
    .setContentText("$progress %% 完了")
    .setProgress(100, progress, false)
    .setOngoing(true)
    .setOnlyAlertOnce(true)  // ★ 必須
    .build()
```

そして同じ ID で `notify(id, ...)` を繰り返すと、通知の **中身がスムーズに置き換わります** (新しい通知としてポップアップしない)。

### 不確定進捗

進捗値が分からない場合 (例: 接続中):

```kotlin
.setProgress(0, 0, true)  // indeterminate
```

→ ぐるぐる回るバーが表示されます。

### 完了通知

```kotlin
NotificationCompat.Builder(context, channelId)
    .setContentTitle("ダウンロード完了")
    .setProgress(0, 0, false)  // バーを除去
    .setOngoing(false)
    .setAutoCancel(true)
    .build()
```

同じ ID で `notify()` すれば置き換わります。

## 本サンプルの実装方針

学習スコープを通知 API に絞るため、進捗ループを **画面の `LaunchedEffect` 内で `delay(500)`** で実装しています:

```kotlin
LaunchedEffect(running) {
    if (!running) return@LaunchedEffect
    while (running && progress < 100) {
        poster.notify(NOTIF_STEP5_PROGRESS, builder.buildProgress(context, progress))
        delay(500)
        progress += 10
    }
}
```

実アプリでは **WorkManager の Foreground Service Worker** や **Foreground Service** で実行するのが正解です。本サンプルは「通知更新の挙動」を見るための簡易実装です。

## 落とし穴 ⚠️

| 症状 | 対策 |
|------|------|
| 更新ごとに音が鳴る | `setOnlyAlertOnce(true)` を忘れている |
| バーが消えない | 完了時に `setProgress(0, 0, false)` で **明示的に除去** |
| 通知が複数表示される | `notify` の id を毎回変えている。**同じ id** で更新すること |
| バックグラウンドで停止する | 本サンプルは画面表示中のみ動く。実アプリは Foreground Service / WorkManager Foreground を使う |

## 関連ファイル

- [NotificationBuilders.buildProgress / buildProgressComplete](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt)
- [Step5Screen.kt](../app/src/main/java/com/example/localnotification/ui/step5/Step5Screen.kt)
