package com.example.localnotification.notification.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Firebase の `FirebaseMessaging.getInstance()` 相当の窓口を学習目的で再現したシングルトン。
 *
 * **責務**:
 * 1. 端末トークンの保持と (再) 発行
 * 2. UI からの「メッセージ擬似受信」リクエストを [MessagingDelegate] に配信
 * 3. トークン更新を [MessagingDelegate.onNewToken] に通知
 *
 * **本物との違い**:
 * - 本物の FCM はネットワークから自動的にメッセージが届くが、ここでは UI から
 *   `simulateMessage()` を呼ぶことで擬似的にメッセージを発火させる。
 * - 本物のトークンは Firebase インスタンス ID から発行されるが、ここでは UUID で代用。
 *
 * **使い方**:
 * ```
 * MessagingMock.setDelegate(MockMessagingService())   // アプリ起動時に 1 回だけ
 * MessagingMock.simulateMessage(context, RemoteMessage(...))  // UI ボタンから
 * ```
 */
object MessagingMock {

    private const val TAG = "MessagingMock"

    private var delegate: MessagingDelegate? = null

    private val _token = MutableStateFlow<String>(generateToken())
    /** 現在の端末トークン。UI が `collectAsStateWithLifecycle` で監視する想定。 */
    val token: StateFlow<String> = _token.asStateFlow()

    /**
     * メッセージ受信ハンドラを差し込む。
     * [com.example.localnotification.LocalNotificationApp] から 1 回だけ呼ぶ。
     */
    fun setDelegate(delegate: MessagingDelegate) {
        this.delegate = delegate
    }

    /**
     * 「サーバーからメッセージが届いた」状態を擬似的に発火させる。
     * 内部的に [MessagingDelegate.onMessageReceived] を呼び出す。
     */
    fun simulateMessage(context: Context, message: RemoteMessage) {
        val d = delegate
        if (d == null) {
            Log.w(TAG, "delegate not set; ignoring message ${message.messageId}")
            return
        }
        d.onMessageReceived(context, message)
    }

    /**
     * トークンを再生成し、[MessagingDelegate.onNewToken] を発火させる。
     *
     * **本物の FCM での発火タイミング**:
     * - 初回起動時
     * - アプリデータ削除時
     * - アプリ再インストール時
     * - 端末側からトークン削除を要求した時
     * - インスタンス ID プロバイダのアップデート時
     */
    fun rotateToken(context: Context) {
        val newToken = generateToken()
        _token.value = newToken
        delegate?.onNewToken(context, newToken)
            ?: Log.w(TAG, "delegate not set; new token not propagated")
    }

    /** 学習用のダミートークン生成。本物は Firebase インスタンス ID 由来の長い文字列。 */
    private fun generateToken(): String = "mock-${UUID.randomUUID()}"
}
