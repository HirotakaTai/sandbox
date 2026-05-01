package com.example.localnotification.notification.remote

import android.content.Context
import android.util.Log
import com.example.localnotification.notification.NotificationBuilders
import com.example.localnotification.notification.NotificationIds
import com.example.localnotification.notification.NotificationPoster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * [MessagingDelegate] のサンプル実装。
 *
 * **本物との対応**:
 * 実プロジェクトでは `class MyMessagingService : FirebaseMessagingService()` の中身に相当する。
 * AndroidManifest.xml に Service として登録し、`<intent-filter>` で
 * `com.google.firebase.MESSAGING_EVENT` を受け取る。
 *
 * **設計判断**:
 * - 通知 ID を AtomicInteger でインクリメントすることで、複数の通知が並列に表示される。
 *   (一斉に同じ ID で発行すると上書きされてしまう)
 * - サーバー登録は IO ディスパッチャで非同期に。本番なら WorkManager を使うのが理想。
 */
class MockMessagingService : MessagingDelegate() {

    private val nextNotifId = AtomicInteger(NotificationIds.NOTIF_STEP8_REMOTE_BASE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(context: Context, message: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: id=${message.messageId} data=${message.data}")

        val notifId = nextNotifId.getAndIncrement()

        // FCM の流儀:
        // - notification + data 両方 → notification を表示し、data はタップ時の deep link 等に利用
        // - data のみ                   → 自前で組み立てて表示
        // - notification のみ          → (実 FCM では OS がバックグラウンド時に自動表示するが) ここでは常に自前表示
        val title = message.notification?.title ?: message.data["title"] ?: DEFAULT_TITLE
        val body = message.notification?.body ?: message.data["body"] ?: DEFAULT_BODY

        val notif = NotificationBuilders.buildRemoteNotification(
            context = context,
            notifId = notifId,
            title = title,
            body = body,
            deepLinkRoute = message.deepLinkRoute,
        )
        NotificationPoster(context).notify(notifId, notif)
    }

    override fun onNewToken(context: Context, token: String) {
        Log.d(TAG, "onNewToken: $token")
        // 取得したトークンは必ずサーバーに送る (これを怠ると個別配信ができない)。
        scope.launch { MockServerApi.registerToken(token) }
    }

    companion object {
        private const val TAG = "MockMessagingSvc"
        private const val DEFAULT_TITLE = "通知"
        private const val DEFAULT_BODY = "新しいメッセージがあります"
    }
}
