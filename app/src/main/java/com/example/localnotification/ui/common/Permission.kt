package com.example.localnotification.ui.common

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.localnotification.R
import com.example.localnotification.notification.NotificationPoster

/**
 * POST_NOTIFICATIONS 権限の状態を保持・操作する Compose state ホルダー。
 *
 * **設計方針**:
 * - Composable 関数として状態と launcher を提供する。
 * - 拒否された場合は設定アプリへの導線を表示する (2 回拒否されると OS が二度と尋ねない)。
 *
 * **API ガード**:
 * - API 32 以下では権限は自動許可されるため、launcher を呼ぶ必要はない。
 *   それでも launcher 自体は安全に呼べる (no-op) ように残してある。
 */
class PermissionState internal constructor(
    val isGranted: Boolean,
    val showRationale: Boolean,
    val request: () -> Unit,
)

@Composable
fun rememberNotificationPermissionState(): PermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(NotificationPoster.hasPostPermission(context)) }
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { result ->
        granted = result
        // false が返ったら 1 回拒否された状態。次回からは UX を考えて rationale を表示。
        showRationale = !result
    }

    return PermissionState(
        isGranted = granted,
        showRationale = showRationale,
        request = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // API 32 以下: マニフェスト宣言で自動許可されるので状態だけ反映。
                granted = true
            }
        },
    )
}

/**
 * 各 Step 画面の上部に表示する権限ステータスカード。
 * 未許可ならリクエスト導線、拒否済みなら設定アプリへの導線を表示する。
 */
@Composable
fun PermissionStatusCard(
    state: PermissionState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (state.isGranted) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.permission_rationale_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    if (state.showRationale) R.string.permission_denied_message
                    else R.string.permission_rationale_message,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.showRationale) {
                // 2 回以上拒否された後はシステムダイアログが二度と表示されないので、
                // 設定画面へ送るしか手段がない。
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.home_open_settings))
                }
            } else {
                Button(onClick = state.request) {
                    Text(stringResource(R.string.permission_request))
                }
            }
        }
    }
}
