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
    /**
     * 何もせずただ表示するだけの最小通知を組み立てる。
     *
     * - `setSmallIcon` は **必須**。指定しないと `notify()` で例外/サイレント失敗になる。
     * - `setAutoCancel(true)` でタップ時に通知が自動的に消える。
     */
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
    /**
     * タップすると [MainActivity] を開いて Step 2 画面へ deep link する通知。
     *
     * @param payload 通知タップ後に画面へ渡す任意の文字列ペイロード。
     *
     * **学習ポイント**:
     * - Trampoline 禁止 (Android 12+) のため Receiver 経由ではなく `getActivity` を直接使う。
     * - `FLAG_IMMUTABLE` は API 31+ で必須。これがないとビルド済み APK でクラッシュする。
     * - `FLAG_UPDATE_CURRENT` は同じ `requestCode` の PendingIntent の Extras を最新値に差し替える。
     */
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
    /** 折りたたみ時は 1 行、展開すると複数行のテキストを表示する `BigTextStyle`。 */
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

    /**
     * 展開時に大きな画像を表示する `BigPictureStyle`。
     * `BigPictureStyle` は Bitmap を必要とするため、ここでは vector drawable を一度 Bitmap にラスタライズしている。
     */
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

    /**
     * 複数行を箇条書き表示する `InboxStyle`。
     * メール一覧やメッセージ一覧の集約表示に向く。
     */
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
    /** 「既読にする」アクションを区別するための Intent action 文字列。 */
    const val ACTION_MARK_READ = "com.example.localnotification.action.MARK_READ"
    /** 「返信」アクション (RemoteInput 入力欄付き) を区別するための Intent action 文字列。 */
    const val ACTION_REPLY = "com.example.localnotification.action.REPLY"
    /** Receiver 側で対象通知を識別するために Intent extra に入れる通知 ID キー。 */
    const val EXTRA_NOTIF_ID = "extra_notif_id"
    /** RemoteInput が返信テキストを格納する Bundle のキー。Builder と取得側で同じ値を使う。 */
    const val KEY_TEXT_REPLY = "key_text_reply"

    /** 通知タップ時に MainActivity が読み取って遷移先を判定するルート文字列キー。 */
    const val EXTRA_NAV_ROUTE = "extra_nav_route"
    /** Step 2 で「タップで運ばれてきたペイロード」を Activity に渡すためのキー。 */
    const val EXTRA_PAYLOAD = "extra_payload"

    /**
     * 「既読にする」アクションと「返信」アクション (RemoteInput 付き) を含む通知を組み立てる。
     *
     * **学習ポイント**:
     * - 既読アクションは `getBroadcast` で BroadcastReceiver に飛ばす (画面を開かないので trampoline 規制対象外)。
     * - 返信アクション側だけは `FLAG_MUTABLE` を使う。OS が RemoteInput の入力テキストを Intent に注入する必要があるため。
     * - 他の PendingIntent は `FLAG_IMMUTABLE` のまま。安易に MUTABLE を広げないこと。
     */
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
    /**
     * 進捗バー付き通知を組み立てる。同じ通知 ID で繰り返し `notify()` することで更新する。
     *
     * @param progress 現在の進捗値 (0..max)。
     * @param max 進捗の最大値。デフォルト 100 (= パーセント表現)。
     *
     * `setOnlyAlertOnce(true)` で更新時に音が鳴らないようにする (頻繁に呼んでも UX を壊さない)。
     */
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

    /**
     * 進捗 100% 到達時に [buildProgress] が出していた通知を「完了」表示で上書きする。
     * `setProgress(0, 0, false)` で進捗バーを消すのが定石。
     */
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
    /**
     * グループに属する子通知を組み立てる。同じ `setGroup(...)` キーを持つ通知が束ねられる。
     *
     * @param sender 表示用の送信者名。
     * @param message 表示用の本文。
     */
    fun buildGroupedChild(context: Context, sender: String, message: String): Notification {
        return NotificationCompat.Builder(context, NotificationIds.CHANNEL_GROUPED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(sender)
            .setContentText(message)
            .setGroup(NotificationIds.GROUP_KEY_STEP6) // 同じ group key で束ねる
            .setAutoCancel(true)
            .build()
    }

    /**
     * グループの「代表」となるサマリー通知。
     * 子通知をすべて `notify()` した **後** にこれを発行することで、Android 7+ の自動グルーピングが正しく働く。
     * `setGroupSummary(true)` を必ず指定すること。これが summary であることの目印になる。
     */
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
    /**
     * [NotificationWorker] が `doWork()` の中で組み立てて発行する予約通知。
     * タップで Step 7 画面に戻れるよう、deep link 用の extra を入れている。
     */
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
