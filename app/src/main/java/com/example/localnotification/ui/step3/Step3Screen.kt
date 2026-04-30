package com.example.localnotification.ui.step3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Step 3: 3 つのスタイル (BigText / BigPicture / Inbox) をそれぞれ試せる画面。
 *
 * @param onBack 戻るボタン押下時のコールバック。
 */
@Composable
fun Step3Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()

    StepScaffold(
        title = stringResource(R.string.step3_title),
        description = stringResource(R.string.step3_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)
        val poster = NotificationPoster(context)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!permission.isGranted) { permission.request(); return@Button }
                    poster.notify(
                        NotificationIds.NOTIF_STEP3_BIG_TEXT,
                        NotificationBuilders.buildBigText(context),
                    )
                },
            ) { Text(stringResource(R.string.step3_big_text_button)) }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!permission.isGranted) { permission.request(); return@Button }
                    poster.notify(
                        NotificationIds.NOTIF_STEP3_BIG_PICTURE,
                        NotificationBuilders.buildBigPicture(context),
                    )
                },
            ) { Text(stringResource(R.string.step3_big_picture_button)) }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (!permission.isGranted) { permission.request(); return@Button }
                    poster.notify(
                        NotificationIds.NOTIF_STEP3_INBOX,
                        NotificationBuilders.buildInbox(context),
                    )
                },
            ) { Text(stringResource(R.string.step3_inbox_button)) }
        }
    }
}
