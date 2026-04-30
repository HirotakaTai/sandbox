package com.example.localnotification.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.localnotification.R
import com.example.localnotification.notification.NotificationBuilders.ACTION_MARK_READ
import com.example.localnotification.notification.NotificationBuilders.ACTION_REPLY
import com.example.localnotification.notification.NotificationBuilders.EXTRA_NOTIF_ID
import com.example.localnotification.notification.NotificationBuilders.KEY_TEXT_REPLY

/**
 * Step 4 の通知アクションを受信する Receiver。
 *
 * **重要 — Notification Trampoline 禁止 (Android 12+)**:
 * BroadcastReceiver / Service の中から Activity を起動すると Android 12 以降は
 * 黙って失敗する (起動制限 + ログのみ)。そのため、この Receiver では
 *  - 通知の更新 / 取消
 *  - in-memory の状態更新 (ViewModel が観測する SharedFlow など)
 * のみを行い、Activity を開きたい場合は通知本体の contentIntent (Activity 直行) で行う。
 *
 * ブロードキャスト受信は数秒以内に完了する必要があるため (ANR 回避)、
 * ここでは I/O などの重い処理を行わない。
 */
class NotificationActionReceiver : BroadcastReceiver() {
    /**
     * 通知のアクション (既読 / 返信) が送られたときに呼ばれる。
     *
     * **重要な制約**:
     * - このメソッドは **メインスレッド + 数秒以内に return** しないと ANR になる。
     *   重い I/O が必要なら `goAsync()` や WorkManager に託すこと。
     * - ここから Activity を起動してはいけない (Trampoline 禁止 / Android 12+)。
     *   画面への遷移が必要なアクションは Receiver ではなく、
     *   通知本体の contentIntent (PendingIntent.getActivity) で表現する。
     */
    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)
        val poster = NotificationPoster(context)
        when (intent.action) {
            ACTION_MARK_READ -> {
                Log.d(TAG, "MARK_READ id=$notifId")
                NotificationEvents.emit(NotificationEvents.Event.MarkedRead(notifId))
                poster.cancel(notifId)
            }
            ACTION_REPLY -> {
                // RemoteInput.getResultsFromIntent: OS が入力テキストを入れて返してくる。
                // この API に渡すのは Receiver に渡されたオリジナルの intent (加工したものではない)。
                val reply = RemoteInput.getResultsFromIntent(intent)
                    ?.getCharSequence(KEY_TEXT_REPLY)
                    ?.toString()
                    .orEmpty()
                Log.d(TAG, "REPLY id=$notifId text=$reply")
                NotificationEvents.emit(NotificationEvents.Event.Replied(notifId, reply))

                // 返信を受け付けたことが視覚的に分かるよう、通知本体を更新する。
                // (アクションは消し、本文を「返信受付済み」に書き換える)
                val updated = NotificationCompat.Builder(context, NotificationIds.CHANNEL_ACTIONS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.step4_notif_title))
                    .setContentText(context.getString(R.string.step4_reply_received, reply))
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .build()
                poster.notify(notifId, updated)
            }
        }
    }

    companion object {
        /** Logcat でフィルタしやすいよう 23 文字以内の TAG を使う (古い Android の制限)。 */
        private const val TAG = "NotifActionReceiver"
    }
}
