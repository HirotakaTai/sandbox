package com.example.localnotification.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Step 7 用の薄いラッパー。
 *
 * **方針**:
 * - "予約は OS の責務" という思想に基づき、自前で Handler / coroutine delay を使わず
 *   必ず WorkManager 経由でスケジュールする (再起動耐性)。
 * - enqueueUniqueWork(REPLACE) を使い、ボタン連打しても 1 件だけが残るようにする。
 */
class WorkScheduler(private val context: Context) {

    /**
     * `seconds` 秒後に通知を発行するジョブをスケジュールする。
     *
     * - WorkManager は **不正確 (inexact)** なため、Doze 中は指定秒数から数分ずれることがある。
     * - REPLACE ポリシーにより、すでに同名の Work があれば古い方をキャンセルして上書きする。
     */
    fun scheduleNotificationIn(seconds: Long) {
        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(seconds, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            NotificationWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /**
     * スケジュールされているジョブをキャンセルする。
     * すでに実行済みだった場合は何も起きない。
     */
    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(NotificationWorker.UNIQUE_WORK_NAME)
    }

    /**
     * UI から observe するための Flow。
     *
     * @return WorkInfo.State をストリームする。未スケジュール時は `null`、
     * それ以外は ENQUEUED / RUNNING / SUCCEEDED / CANCELLED などの状態。
     * Compose 側では `collectAsStateWithLifecycle` と組み合わせて使う。
     */
    fun observeState(): Flow<WorkInfo.State?> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(NotificationWorker.UNIQUE_WORK_NAME)
            .map { list -> list.firstOrNull()?.state }
    }
}
