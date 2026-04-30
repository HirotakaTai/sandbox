package com.example.localnotification.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Step 7: 予約された時間に通知を発行する Worker。
 *
 * **WorkManager を選ぶ理由 (vs AlarmManager / JobScheduler)**:
 * - 端末が再起動されても延期されたジョブが復元される。
 * - Doze / App Standby などの省電力機能と協調する (inexact)。
 * - SCHEDULE_EXACT_ALARM 権限 (Android 14 以降ユーザー操作が必要) を
 *   取らずに済むため、ユーザー体験が良い。
 *
 * **inexact の意味**:
 * - WorkManager の OneTimeWorkRequest + initialDelay は **おおよそ N 秒後** に動く。
 *   Doze 中などはずれることがある。アラームクロックのような「正確に X 時 Y 分」が
 *   必須な要件は AlarmManager.setExactAndAllowWhileIdle を使う必要があるが、
 *   学習用の本サンプルでは扱わない。
 *
 * **CoroutineWorker を使う理由**:
 * - doWork が suspend 関数として書けるため、長時間処理を coroutine の流儀で書ける。
 * - 本 Worker は通知を 1 発行するだけで I/O はないため、suspend である必要は薄いが、
 *   将来的に I/O を足す際の拡張性のため CoroutineWorker を採用している。
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val poster = NotificationPoster(applicationContext)
        // 権限が無ければ post は no-op。WorkManager 自体は成功扱いにして再試行を防ぐ。
        val notification = NotificationBuilders.buildScheduled(applicationContext)
        poster.notify(NotificationIds.NOTIF_STEP7_SCHEDULED, notification)
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "step7_scheduled_notification"
    }
}
