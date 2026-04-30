package com.example.localnotification.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.example.localnotification.R

/**
 * すべての NotificationChannel をまとめて作成する責務を持つ。
 *
 * NotificationChannelCompat を使う理由:
 * - API 26 (Oreo) 未満では内部的に no-op となるため、SDK バージョンガードを書かずに済む。
 * - androidx.core 由来なので将来の API 変更に追従しやすい。
 *
 * importance の選び方 (NotificationManagerCompat 定数 = NotificationManager 定数):
 * - MIN(1): バッジも音もなし (背景同期完了など)
 * - LOW(2): 音もバイブもなし (補足情報)
 * - DEFAULT(3): 音あり、ヘッドアップなし
 * - HIGH(4): ヘッドアップ通知 (ユーザーの作業を中断するので慎重に)
 */
object NotificationChannelRegistrar {

    fun registerAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)

        val basic = NotificationChannelCompat.Builder(
            NotificationIds.CHANNEL_BASIC,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
            .setName(context.getString(R.string.channel_basic_name))
            .setDescription(context.getString(R.string.channel_basic_desc))
            .build()

        // アクション通知は会話的なので少し目立たせる (HIGH = ヘッドアップ)
        val actions = NotificationChannelCompat.Builder(
            NotificationIds.CHANNEL_ACTIONS,
            NotificationManagerCompat.IMPORTANCE_HIGH,
        )
            .setName(context.getString(R.string.channel_actions_name))
            .setDescription(context.getString(R.string.channel_actions_desc))
            .build()

        val grouped = NotificationChannelCompat.Builder(
            NotificationIds.CHANNEL_GROUPED,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
            .setName(context.getString(R.string.channel_grouped_name))
            .setDescription(context.getString(R.string.channel_grouped_desc))
            .build()

        val scheduled = NotificationChannelCompat.Builder(
            NotificationIds.CHANNEL_SCHEDULED,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
            .setName(context.getString(R.string.channel_scheduled_name))
            .setDescription(context.getString(R.string.channel_scheduled_desc))
            .build()

        // createNotificationChannelsCompat は同 ID なら no-op なので毎回呼んで OK。
        manager.createNotificationChannelsCompat(listOf(basic, actions, grouped, scheduled))
    }
}
