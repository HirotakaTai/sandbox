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

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(NotificationWorker.UNIQUE_WORK_NAME)
    }

    /**
     * UI から observe するための State Flow。
     * `null` は未スケジュール、それ以外は最新の WorkInfo.State を返す。
     */
    fun observeState(): Flow<WorkInfo.State?> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(NotificationWorker.UNIQUE_WORK_NAME)
            .map { list -> list.firstOrNull()?.state }
    }
}
