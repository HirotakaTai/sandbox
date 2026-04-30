# Step 7 — 予約通知 (WorkManager)

将来の時刻に通知を発行します。**WorkManager** を使うことで再起動耐性と Doze モード協調を OS 任せにできます。

## 学ぶこと

1. **`OneTimeWorkRequest` + `setInitialDelay`** で N 秒後に実行
2. **`enqueueUniqueWork(REPLACE)`** でボタン連打しても 1 件にする
3. **`getWorkInfosForUniqueWorkFlow`** で実行状態を Compose に流す
4. WorkManager の **inexact** な動作と **AlarmManager** との使い分け

## 実装ポイント

### スケジュール

```kotlin
val request = OneTimeWorkRequestBuilder<NotificationWorker>()
    .setInitialDelay(10, TimeUnit.SECONDS)
    .build()
WorkManager.getInstance(context).enqueueUniqueWork(
    UNIQUE_WORK_NAME,
    ExistingWorkPolicy.REPLACE,
    request,
)
```

### Worker

```kotlin
class NotificationWorker(ctx: Context, params: WorkerParameters)
    : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val poster = NotificationPoster(applicationContext)
        poster.notify(NOTIF_STEP7_SCHEDULED, builder.buildScheduled(applicationContext))
        return Result.success()
    }
}
```

### 状態を UI に流す

```kotlin
val state by scheduler.observeState().collectAsStateWithLifecycle(initialValue = null)
// state は WorkInfo.State? (ENQUEUED, RUNNING, SUCCEEDED, ...)
```

## なぜ AlarmManager ではなく WorkManager か

| 観点 | WorkManager | AlarmManager |
|------|-------------|--------------|
| 再起動跨ぎ | OS が自動復元 | `BOOT_COMPLETED` Receiver を自前実装 |
| Doze 協調 | 自動 | 自分で `setAndAllowWhileIdle` |
| 精度 | inexact (数分ずれる可能性) | exact (`setExactAndAllowWhileIdle`) |
| 権限 | 不要 | Android 14+ で `SCHEDULE_EXACT_ALARM` 要 (UX が悪い) |
| 学習コスト | 低 | 中 |

→ "アラームクロック" や "正確な時刻 X 時 Y 分" が必要なケース以外は **WorkManager 一択**。

## 落とし穴 ⚠️

| 症状 | 対策 |
|------|------|
| ボタン連打で複数スケジュールされる | `enqueueUniqueWork(REPLACE)` を使う |
| 通知が出ない | `POST_NOTIFICATIONS` 権限が無いとサイレント失敗。`hasPostPermission` でチェック |
| 10 秒丁度に出ない | inexact なので想定動作。Doze 中は数分遅延することも |
| キャンセルできない | `cancelUniqueWork` を呼ぶ。`cancel()` (id 指定) ではない |

## 関連ファイル

- [WorkScheduler.kt](../app/src/main/java/com/example/localnotification/notification/WorkScheduler.kt)
- [NotificationWorker.kt](../app/src/main/java/com/example/localnotification/notification/NotificationWorker.kt)
- [NotificationBuilders.buildScheduled](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt)
- [Step7Screen.kt](../app/src/main/java/com/example/localnotification/ui/step7/Step7Screen.kt)
