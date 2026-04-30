package com.example.localnotification.ui.step2

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.localnotification.R
import com.example.localnotification.notification.NotificationBuilders
import com.example.localnotification.notification.NotificationIds
import com.example.localnotification.notification.NotificationPoster
import com.example.localnotification.ui.common.PermissionStatusCard
import com.example.localnotification.ui.common.StepScaffold
import com.example.localnotification.ui.common.rememberNotificationPermissionState

@Composable
fun Step2Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()
    var counter by remember { mutableIntStateOf(0) }

    StepScaffold(
        title = stringResource(R.string.step2_title),
        description = stringResource(R.string.step2_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)
        Text(
            text = stringResource(R.string.step2_received_payload, "tap-#$counter"),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                if (!permission.isGranted) {
                    permission.request()
                    return@Button
                }
                counter += 1
                val poster = NotificationPoster(context)
                poster.notify(
                    NotificationIds.NOTIF_STEP2_TAP,
                    NotificationBuilders.buildTappable(context, payload = "tap-#$counter"),
                )
            },
        ) {
            Text(stringResource(R.string.step2_show_button))
        }
    }
}
