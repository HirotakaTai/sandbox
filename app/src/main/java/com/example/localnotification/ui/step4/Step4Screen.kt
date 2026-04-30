package com.example.localnotification.ui.step4

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.localnotification.R
import com.example.localnotification.notification.NotificationBuilders
import com.example.localnotification.notification.NotificationEvents
import com.example.localnotification.notification.NotificationIds
import com.example.localnotification.notification.NotificationPoster
import com.example.localnotification.ui.common.PermissionStatusCard
import com.example.localnotification.ui.common.StepScaffold
import com.example.localnotification.ui.common.rememberNotificationPermissionState

@Composable
fun Step4Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()
    var lastEvent by remember { mutableStateOf<String?>(null) }

    // Receiver から流れてくるイベントを観測 (collect は LaunchedEffect でライフサイクル管理)
    LaunchedEffect(Unit) {
        NotificationEvents.events.collect { event ->
            lastEvent = when (event) {
                is NotificationEvents.Event.MarkedRead ->
                    context.getString(R.string.step4_marked_read)
                is NotificationEvents.Event.Replied ->
                    context.getString(R.string.step4_reply_received, event.text)
            }
        }
    }

    StepScaffold(
        title = stringResource(R.string.step4_title),
        description = stringResource(R.string.step4_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)
        Text(
            text = lastEvent ?: "—",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                if (!permission.isGranted) { permission.request(); return@Button }
                NotificationPoster(context).notify(
                    NotificationIds.NOTIF_STEP4_ACTIONS,
                    NotificationBuilders.buildWithActions(context),
                )
            },
        ) {
            Text(stringResource(R.string.step4_show_button))
        }
    }
}
