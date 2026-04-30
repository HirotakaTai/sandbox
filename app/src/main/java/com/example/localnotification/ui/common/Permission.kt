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
 * POST_NOTIFICATIONS 権限の状態と launcher をまとめたデータクラス。
 *
 * @property isGranted 現在権限が許可されているか。
 * @property showRationale 1 回以上拒否されたか (= rationale を見せるべきか)。
 * @property request システムの権限ダイアログを起動するトリガー。API 32 以下ではスタブとして許可扱いにする。
 */
class PermissionState internal constructor(
    val isGranted: Boolean,
    val showRationale: Boolean,
    val request: () -> Unit,
)

/**
 * Composable から使える POST_NOTIFICATIONS 権限ホルダーを生成する。
 *
 * **学習ポイント**:
 * - `rememberLauncherForActivityResult` はコンポジションのライフサイクルに紐付く、
 *   Activity の onCreate 以前に launcher を登録しておく面倒な作業を Compose が代行してくれる。
 * - 2 回連続で拒否されると OS は以降ダイアログを表示しないため、
 *   ユーザーを設定アプリへ誘導する動線が必要になる ([PermissionStatusCard] を参照)。
 */
@Composable
fun rememberNotificationPermissionState(): PermissionState {
    val context = LocalContext.current
    // remember + mutableStateOf: コンポジション間で状態を保持し、値が変われば再コンポジションさせる。
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
 * 権限未許可時に画面上部に表示するステータスカード。
 *
 * - `state.isGranted == true` なら何も描画しない (early return)。
 * - 1 回拒否済みのときは、システムダイアログが二度と出ないため設定アプリへの導線に切り替える。
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
