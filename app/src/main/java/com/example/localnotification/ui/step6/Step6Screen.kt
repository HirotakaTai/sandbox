package com.example.localnotification.ui.step6

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val sampleMessages = listOf(
    "tanaka" to "明日の MTG ですが…",
    "suzuki" to "議事録を共有しました。",
    "yamada" to "例の件、進捗どうですか?",
)

@Composable
fun Step6Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()

    StepScaffold(
        title = stringResource(R.string.step6_title),
        description = stringResource(R.string.step6_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (!permission.isGranted) { permission.request(); return@Button }
                    val poster = NotificationPoster(context)
                    sampleMessages.forEachIndexed { index, (sender, msg) ->
                        poster.notify(
                            NotificationIds.NOTIF_STEP6_CHILD_BASE + index,
                            NotificationBuilders.buildGroupedChild(context, sender, msg),
                        )
                    }
                    // Summary は子の **後** に発行することで Android 7+ の自動グルーピングが正しく働く
                    poster.notify(
                        NotificationIds.NOTIF_STEP6_SUMMARY,
                        NotificationBuilders.buildGroupedSummary(context),
                    )
                },
            ) { Text(stringResource(R.string.step6_show_button)) }

            Button(
                onClick = {
                    val poster = NotificationPoster(context)
                    sampleMessages.indices.forEach {
                        poster.cancel(NotificationIds.NOTIF_STEP6_CHILD_BASE + it)
                    }
                    poster.cancel(NotificationIds.NOTIF_STEP6_SUMMARY)
                },
            ) { Text(stringResource(R.string.step6_clear_button)) }
        }
    }
}
