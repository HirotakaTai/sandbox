package com.example.localnotification.ui.step5

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.localnotification.R
import com.example.localnotification.notification.NotificationBuilders
import com.example.localnotification.notification.NotificationIds
import com.example.localnotification.notification.NotificationPoster
import com.example.localnotification.ui.common.PermissionStatusCard
import com.example.localnotification.ui.common.StepScaffold
import com.example.localnotification.ui.common.rememberNotificationPermissionState
import kotlinx.coroutines.delay

@Composable
fun Step5Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        val poster = NotificationPoster(context)
        progress = 0
        while (running && progress < 100) {
            poster.notify(
                NotificationIds.NOTIF_STEP5_PROGRESS,
                NotificationBuilders.buildProgress(context, progress),
            )
            delay(500)
            progress += 10
        }
        if (running) {
            poster.notify(
                NotificationIds.NOTIF_STEP5_PROGRESS,
                NotificationBuilders.buildProgressComplete(context),
            )
        }
        running = false
    }

    StepScaffold(
        title = stringResource(R.string.step5_title),
        description = stringResource(R.string.step5_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)
        Text(stringResource(R.string.step5_notif_text_progress, progress))
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (!permission.isGranted) { permission.request(); return@Button }
                    running = true
                },
                enabled = !running,
            ) { Text(stringResource(R.string.step5_start_button)) }

            Button(
                onClick = {
                    running = false
                    NotificationPoster(context).cancel(NotificationIds.NOTIF_STEP5_PROGRESS)
                },
                enabled = running,
            ) { Text(stringResource(R.string.step5_cancel_button)) }
        }
    }
}
