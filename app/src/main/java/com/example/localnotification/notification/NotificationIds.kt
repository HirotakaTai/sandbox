package com.example.localnotification.notification

/**
 * アプリで利用する Notification Channel の ID と通知 ID の集約。
 *
 * **チャンネル設計の指針**:
 * - 利用シーン (importance / sound / vibration を変えたい単位) で分割する。
 *   「Step ごとに 1 チャンネル」のような開発者都合の分割は避けるのが本来の作法だが、
 *   学習用に importance の違いを体感できるよう用途別に 4 つに分けている。
 * - 一度ユーザーに表示したチャンネル ID は変更してはならない (新規 ID として扱われ、
 *   ユーザーが調整した設定が失われる)。
 *
 * **通知 ID 設計**:
 * - 同一 ID の notify() は更新動作になる (Step 5 の進捗通知で活用)。
 * - グループ通知 (Step 6) は子要素ごとにユニーク ID を振り、サマリーは別 ID。
 */
object NotificationIds {

    // ===== Channel IDs =====
    const val CHANNEL_BASIC = "channel_basic"        // Step 1〜3, 5
    const val CHANNEL_ACTIONS = "channel_actions"    // Step 4
    const val CHANNEL_GROUPED = "channel_grouped"    // Step 6
    const val CHANNEL_SCHEDULED = "channel_scheduled" // Step 7

    // ===== Notification IDs =====
    const val NOTIF_STEP1_BASIC = 1001
    const val NOTIF_STEP2_TAP = 1002
    const val NOTIF_STEP3_BIG_TEXT = 1003
    const val NOTIF_STEP3_BIG_PICTURE = 1004
    const val NOTIF_STEP3_INBOX = 1005
    const val NOTIF_STEP4_ACTIONS = 1006
    const val NOTIF_STEP5_PROGRESS = 1007

    // Step 6 (グループ): サマリー + 子 3 件の例
    const val NOTIF_STEP6_SUMMARY = 1100
    const val NOTIF_STEP6_CHILD_BASE = 1101 // 1101, 1102, 1103
    const val GROUP_KEY_STEP6 = "group_step6_messages"

    const val NOTIF_STEP7_SCHEDULED = 1200
}
