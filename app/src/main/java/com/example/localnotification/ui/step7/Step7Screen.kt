package com.example.localnotification.ui.step7

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.example.localnotification.R
import com.example.localnotification.notification.WorkScheduler
import com.example.localnotification.ui.common.PermissionStatusCard
import com.example.localnotification.ui.common.StepScaffold
import com.example.localnotification.ui.common.rememberNotificationPermissionState

/**
 * Step 7: WorkManager を使った 10 秒後の予約通知を試す画面。
 *
 * **学習ポイント**:
 * - `collectAsStateWithLifecycle` は Activity/Fragment のライフサイクルを考慮して collect を自動で
 *   中断/再開するため、バックグラウンドで不必要なリソースを使わない (`collectAsState` より推奨)。
 * - WorkManager は Doze 中も動くが、タイミングは不正確 (指定秒数から数分ずれることがある)。
 *
 * @param onBack 戻るボタン押下時のコールバック。
 */
@Composable
fun Step7Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()
    // remember(scheduler): scheduler インスタンスをコンポジション間で保持し、再生成を防ぐ。
    val scheduler = remember { WorkScheduler(context) }
    val state by scheduler.observeState().collectAsStateWithLifecycle(initialValue = null)

    StepScaffold(
        title = stringResource(R.string.step7_title),
        description = stringResource(R.string.step7_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)
        Text(
            text = stringResource(state.toLabelRes()),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (!permission.isGranted) { permission.request(); return@Button }
                    scheduler.scheduleNotificationIn(seconds = 10)
                },
            ) { Text(stringResource(R.string.step7_schedule_button)) }
            Button(
                onClick = { scheduler.cancel() },
            ) { Text(stringResource(R.string.step7_cancel_button)) }
        }
    }
}

/** WorkInfo の状態を画面表示用の文字列リソース ID に変換する拡張関数。 */
private fun WorkInfo.State?.toLabelRes(): Int = when (this) {
    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> R.string.step7_status_enqueued
    WorkInfo.State.RUNNING -> R.string.step7_status_running
    WorkInfo.State.SUCCEEDED -> R.string.step7_status_succeeded
    WorkInfo.State.CANCELLED, WorkInfo.State.FAILED -> R.string.step7_status_cancelled
    null -> R.string.step7_status_idle
}
