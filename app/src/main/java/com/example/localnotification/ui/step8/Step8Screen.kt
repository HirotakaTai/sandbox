package com.example.localnotification.ui.step8

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localnotification.LocalNotificationApp
import com.example.localnotification.R
import com.example.localnotification.notification.remote.MessagingMock
import com.example.localnotification.notification.remote.RemoteMessage
import com.example.localnotification.notification.remote.TopicRegistry
import com.example.localnotification.ui.Routes
import com.example.localnotification.ui.common.PermissionStatusCard
import com.example.localnotification.ui.common.StepScaffold
import com.example.localnotification.ui.common.rememberNotificationPermissionState

/**
 * Step 8: リモートプッシュ通知 (FCM モック) を試す画面。
 *
 * **学習ポイント**:
 * - 実プロジェクトでは `FirebaseMessaging.getInstance().token` でトークン取得、
 *   `FirebaseMessagingService.onMessageReceived` で受信、`subscribeToTopic` でトピック購読。
 *   本サンプルは Firebase 依存を入れず、同等のシグネチャで MessagingMock が再現する。
 * - 「データメッセージ」を擬似発火するボタンを使えば、deep link で Step 2 画面へ遷移する挙動も
 *   学べる (FCM の `data: { route: "step2" }` ペイロードを想定)。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Step8Screen(onBack: () -> Unit) {
    val context = LocalContext.current
    val permission = rememberNotificationPermissionState()
    val clipboard: ClipboardManager = LocalClipboardManager.current

    // Application から TopicRegistry を取得 (Hilt なしの学習用配線)。
    val topicRegistry: TopicRegistry = remember {
        (context.applicationContext as LocalNotificationApp).topicRegistry
    }

    val token by MessagingMock.token.collectAsStateWithLifecycle()
    val topics by topicRegistry.topics.collectAsStateWithLifecycle()

    var topicInput by remember { mutableStateOf("news") }

    StepScaffold(
        title = stringResource(R.string.step8_title),
        description = stringResource(R.string.step8_description),
        onBack = onBack,
    ) {
        PermissionStatusCard(state = permission)

        // ============= トークン表示 =============
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.step8_token_label),
                    style = MaterialTheme.typography.titleSmall,
                )
                // トークンは長いので折り返し表示。タップでクリップボードにコピー。
                ClickableText(
                    text = AnnotatedString(token),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    onClick = { clipboard.setText(AnnotatedString(token)) },
                )
                Button(onClick = { MessagingMock.rotateToken(context) }) {
                    Text(stringResource(R.string.step8_regenerate_token))
                }
            }
        }

        // ============= トピック購読 =============
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = { topicInput = it },
                    label = { Text(stringResource(R.string.step8_topic_input_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (topicInput.isNotBlank()) topicRegistry.subscribe(topicInput.trim())
                        },
                    ) { Text(stringResource(R.string.step8_subscribe)) }
                    OutlinedButton(
                        onClick = {
                            if (topicInput.isNotBlank()) topicRegistry.unsubscribe(topicInput.trim())
                        },
                    ) { Text(stringResource(R.string.step8_unsubscribe)) }
                }
                if (topics.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.step8_subscribed_topics),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        topics.forEach { t ->
                            AssistChip(onClick = { topicInput = t }, label = { Text(t) })
                        }
                    }
                }
            }
        }

        // ============= 擬似メッセージ受信 =============
        val notifTitle = stringResource(R.string.step8_remote_notif_title)
        val notifBody = stringResource(R.string.step8_remote_notif_body)
        Button(
            onClick = {
                if (!permission.isGranted) { permission.request(); return@Button }
                // 通知メッセージの擬似発火 (FCM の "notification" フィールド相当)。
                MessagingMock.simulateMessage(
                    context,
                    RemoteMessage(
                        notification = RemoteMessage.Notification(notifTitle, notifBody),
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.step8_simulate_notification_msg)) }

        OutlinedButton(
            onClick = {
                if (!permission.isGranted) { permission.request(); return@OutlinedButton }
                // データメッセージ + deep link の擬似発火 (FCM の "data: { route: ... }" 相当)。
                MessagingMock.simulateMessage(
                    context,
                    RemoteMessage(
                        data = mapOf(
                            "route" to Routes.STEP2,
                            "title" to notifTitle,
                            "body" to "タップで Step 2 に遷移します",
                        ),
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.step8_simulate_data_msg)) }
    }
}
