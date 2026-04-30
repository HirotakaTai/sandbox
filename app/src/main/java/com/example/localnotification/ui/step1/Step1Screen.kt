package com.example.localnotification.ui.step1

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun Step1Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()

    StepScaffold(
        title = stringResource(R.string.step1_title),
        description = stringResource(R.string.step1_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)
        Button(
            onClick = {
                if (!permission.isGranted) {
                    permission.request()
                    return@Button
                }
                val poster = NotificationPoster(context)
                poster.notify(
                    NotificationIds.NOTIF_STEP1_BASIC,
                    NotificationBuilders.buildBasic(context),
                )
            },
        ) {
            Text(stringResource(R.string.step1_show_button))
        }
    }
}
