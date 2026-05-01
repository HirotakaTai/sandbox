package com.example.localnotification.notification.remote

import android.content.Context

/**
 * FCM の `FirebaseMessagingService` に相当する抽象。
 *
 * **学習目的**:
 * Firebase 依存を入れずに、本物の Service が持つ 2 つのコールバックの形を学ぶ。
 * 実プロジェクトでは:
 * ```
 * class MyMessagingService : FirebaseMessagingService() {
 *     override fun onMessageReceived(message: RemoteMessage) { ... }
 *     override fun onNewToken(token: String) { ... }
 * }
 * ```
 * のように Service として宣言し、AndroidManifest.xml に登録することで OS が自動的に呼び出す。
 * 本サンプルでは Service 化せず、[MessagingMock] が同じシグネチャで配信することで挙動を再現する。
 */
abstract class MessagingDelegate {

    /**
     * メッセージを受信したときに呼ばれる。
     *
     * **重要な制約 (本物の Service 同様)**:
     * - メインスレッド + 約 20 秒以内に処理を完了する必要がある。
     *   重い処理は WorkManager に委譲すること (FCM ペイロードを Worker の input data に詰める)。
     * - データメッセージの場合、ここで [com.example.localnotification.notification.NotificationPoster]
     *   などを使って自分で通知を発行する必要がある。
     */
    abstract fun onMessageReceived(context: Context, message: RemoteMessage)

    /**
     * 端末トークンが (再) 発行されたときに呼ばれる。
     *
     * **重要な責務**:
     * - 取得したトークンを **必ず自社サーバーに送信** して保存する。
     *   そうしないと「特定ユーザー宛て送信」ができない。
     * - 古いトークンは自動的に失効するので、サーバー側は最新トークンで上書きすればよい。
     * - 失敗時はリトライ機構 (WorkManager 等) を用意するのが定石。
     */
    abstract fun onNewToken(context: Context, token: String)
}
