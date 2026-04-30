package com.example.localnotification.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.graphics.drawable.IconCompat
import com.example.localnotification.MainActivity
import com.example.localnotification.R

/**
 * 各 Step が利用する [Notification] を構築する純粋関数の集約。
 *
 * **設計方針**:
 * - notify() の責務は [NotificationPoster] が持ち、ここではビルドのみ。
 *   こうすることでビルド処理を unit test しやすくなる。
 * - PendingIntent の flag は **必ず** FLAG_IMMUTABLE を指定する (Android 12+ 必須)。
 *   FLAG_MUTABLE を必要とするのは RemoteInput の返信 PendingIntent だけ。
 * - Trampoline 禁止 (Android 12+) のため、タップで Activity を開く場合は
 *   PendingIntent.getActivity を使い、Receiver/Service を経由しない。
 */
object NotificationBuilders {

    // ====================================================
    // Step 1: 最小構成の通知
    // ====================================================
    fun buildBasic(context: Context): Notification {
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
            // setSmallIcon は必須。これが無いと post 自体が失敗する。
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step1_notif_title))
            .setContentText(context.getString(R.string.step1_notif_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // API 25 以下のフォールバック
            .setAutoCancel(true)
            .build()
    }

    // ====================================================
    // Step 2: タップで Activity を開く通知
    // ====================================================
    fun buildTappable(context: Context, payload: String): Notification {
        // Activity を直接開く (Trampoline 禁止)。
        val intent = Intent(context, MainActivity::class.java).apply {
            // SINGLE_TOP + CLEAR_TOP: 既存スタックがあれば再利用し、それ以上は破棄。
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_ROUTE, "step2")
            putExtra(EXTRA_PAYLOAD, payload)
        }
        // FLAG_IMMUTABLE は Android 12 (API 31) 以降で必須 (省略するとクラッシュ)。
        // FLAG_UPDATE_CURRENT で同一 requestCode の Extras を最新に更新する。
        val pi = PendingIntent.getActivity(
            context,
            /* requestCode = */ NotificationIds.NOTIF_STEP2_TAP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step2_notif_title))
            .setContentText(context.getString(R.string.step2_notif_text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
    }

    // ====================================================
    // Step 3: 各種スタイル
    // ====================================================
    fun buildBigText(context: Context): Notification {
        val style = NotificationCompat.BigTextStyle()
            .setBigContentTitle(context.getString(R.string.step3_big_text_title))
            .setSummaryText(context.getString(R.string.step3_big_text_summary))
            .bigText(context.getString(R.string.step3_big_text_body))
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step3_big_text_title))
            .setContentText(context.getString(R.string.step3_big_text_body))
            .setStyle(style)
            .setAutoCancel(true)
            .build()
    }

    fun buildBigPicture(context: Context): Notification {
        // ベクタードローアブルを Bitmap 化 (BigPictureStyle は Bitmap 必須)。
        val drawable = checkNotNull(
            androidx.core.content.ContextCompat.getDrawable(context, R.drawable.sample_big_picture),
        ) { "sample_big_picture drawable not found" }
        val bitmap = android.graphics.Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            android.graphics.Bitmap.Config.ARGB_8888,
        ).also { bm ->
            val canvas = android.graphics.Canvas(bm)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        val style = NotificationCompat.BigPictureStyle()
            .bigPicture(bitmap)
            .setBigContentTitle(context.getString(R.string.step3_picture_title))
            .setSummaryText(context.getString(R.string.step3_picture_summary))
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step3_picture_title))
            .setContentText(context.getString(R.string.step3_picture_summary))
            .setStyle(style)
            .setAutoCancel(true)
            .build()
    }

    fun buildInbox(context: Context): Notification {
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(context.getString(R.string.step3_inbox_title))
            .setSummaryText(context.getString(R.string.step3_inbox_summary))
            .addLine("田中: 明日の MTG ですが…")
            .addLine("鈴木: 議事録を共有しました。")
            .addLine("山田: 例の件、進捗どうですか?")
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step3_inbox_title))
            .setContentText(context.getString(R.string.step3_inbox_summary))
            .setStyle(style)
            .setNumber(3) // バッジ件数 (Launcher による)
            .setAutoCancel(true)
            .build()
    }

    // ====================================================
    // Step 4: アクション + RemoteInput (返信)
    // ====================================================
    const val ACTION_MARK_READ = "com.example.localnotification.action.MARK_READ"
    const val ACTION_REPLY = "com.example.localnotification.action.REPLY"
    const val EXTRA_NOTIF_ID = "extra_notif_id"
    const val KEY_TEXT_REPLY = "key_text_reply"

    // Activity ↔ Step 起動用の追加 Extras (Step 2 / Step 4 で利用)
    const val EXTRA_NAV_ROUTE = "extra_nav_route"
    const val EXTRA_PAYLOAD = "extra_payload"

    fun buildWithActions(context: Context): Notification {
        // 1) 既読アクション → BroadcastReceiver で完結 (Activity を開かない = trampoline 違反にならない)
        val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_NOTIF_ID, NotificationIds.NOTIF_STEP4_ACTIONS)
        }
        val markReadPi = PendingIntent.getBroadcast(
            context,
            /* requestCode = */ 4_001,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // 2) 返信アクション → RemoteInput で入力欄を inline 表示
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.step4_reply_label))
            .build()

        val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_NOTIF_ID, NotificationIds.NOTIF_STEP4_ACTIONS)
        }
        // RemoteInput は値を Intent に注入するため FLAG_MUTABLE が必須 (API 31+)。
        val replyPi = PendingIntent.getBroadcast(
            context,
            /* requestCode = */ 4_002,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        val replyAction = NotificationCompat.Action.Builder(
            IconCompat.createWithResource(context, R.drawable.ic_notification),
            context.getString(R.string.step4_action_reply),
            replyPi,
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_ACTIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step4_notif_title))
            .setContentText(context.getString(R.string.step4_notif_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 25 以下フォールバック
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.step4_action_mark_read),
                markReadPi,
            )
            .addAction(replyAction)
            .setAutoCancel(false) // 返信フローでは自動消去させない
            .build()
    }

    // ====================================================
    // Step 5: 進捗通知
    // ====================================================
    fun buildProgress(context: Context, progress: Int, max: Int = 100): Notification {
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step5_notif_title))
            .setContentText(context.getString(R.string.step5_notif_text_progress, progress))
            // setProgress(max, current, indeterminate)
            .setProgress(max, progress, false)
            .setOngoing(true) // ユーザーが手でスワイプ消去できないようにする
            .setOnlyAlertOnce(true) // 更新時に音/バイブを鳴らさない (重要)
            .build()
    }

    fun buildProgressComplete(context: Context): Notification {
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_BASIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step5_notif_complete))
            // setProgress(0, 0, false) で進捗バーを除去
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
    }

    // ====================================================
    // Step 6: グループ通知
    // ====================================================
    fun buildGroupedChild(context: Context, sender: String, message: String): Notification {
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_GROUPED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(sender)
            .setContentText(message)
            .setGroup(NotificationIds.GROUP_KEY_STEP6) // 同じ group key で束ねる
            .setAutoCancel(true)
            .build()
    }

    fun buildGroupedSummary(context: Context): Notification {
        // サマリー通知: グループの代表として表示される (Android 7+ で自動展開可)。
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(context.getString(R.string.step6_summary_title))
            .setSummaryText(context.getString(R.string.step6_summary_text))
            .addLine("田中: 明日の MTG ですが…")
            .addLine("鈴木: 議事録を共有しました。")
            .addLine("山田: 例の件、進捗どうですか?")
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_GROUPED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step6_summary_title))
            .setContentText(context.getString(R.string.step6_summary_text))
            .setStyle(style)
            .setGroup(NotificationIds.GROUP_KEY_STEP6)
            .setGroupSummary(true) // ★これがサマリーであることを宣言
            .setAutoCancel(true)
            .build()
    }

    // ====================================================
    // Step 7: 予約通知 (WorkManager から呼び出される)
    // ====================================================
    fun buildScheduled(context: Context): Notification {
        // 通知タップで MainActivity に戻す (ペイロードで Step 7 画面に遷移)。
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_ROUTE, "step7")
        }
        val pi = PendingIntent.getActivity(
            context,
            /* requestCode = */ NotificationIds.NOTIF_STEP7_SCHEDULED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_SCHEDULED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.step7_notif_title))
            .setContentText(context.getString(R.string.step7_notif_text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
    }
}
