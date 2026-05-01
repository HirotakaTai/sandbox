package com.example.localnotification.notification.remote

/**
 * FCM (Firebase Cloud Messaging) の `com.google.firebase.messaging.RemoteMessage` を
 * 学習目的で簡略化した自前モデル。
 *
 * **本物の FCM ペイロード構造との対応**:
 * - `notification` フィールド: タイトル/本文を持つ「通知メッセージ」。
 *   FCM では **アプリがバックグラウンド時** は OS が自動的にシステムトレイに表示する。
 *   フォアグラウンド時のみ `onMessageReceived` に届く。
 * - `data` フィールド: 任意のキー/値ペアを持つ「データメッセージ」。
 *   常に `onMessageReceived` に届き、自分で通知表示する責務を負う。
 * - `notification` と `data` は同一メッセージ内に **両方含めることもできる**。
 *
 * @property notification 通知メッセージ部分。null ならデータメッセージのみ。
 * @property data 任意のキー/値ペア。空 Map でもよい。
 * @property messageId サーバーが採番した一意 ID (本物の FCM では再送制御に使う)。
 */
data class RemoteMessage(
    val notification: Notification? = null,
    val data: Map<String, String> = emptyMap(),
    val messageId: String = "msg-${System.currentTimeMillis()}",
) {
    /**
     * 通知メッセージ部分。FCM の `RemoteMessage.Notification` に相当する。
     *
     * @property title ステータスバーに表示するタイトル。
     * @property body 通知の本文。
     */
    data class Notification(
        val title: String,
        val body: String,
    )

    /**
     * `data` から「タップ時の遷移先 route」を取り出す独自規約。
     * 本物の FCM でも click_action や独自キーで同様の慣習を取ることが多い。
     */
    val deepLinkRoute: String? get() = data["route"]
}
