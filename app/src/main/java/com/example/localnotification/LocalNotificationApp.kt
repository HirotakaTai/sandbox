package com.example.localnotification

import android.app.Application
import com.example.localnotification.notification.NotificationChannelRegistrar
import com.example.localnotification.notification.remote.MessagingMock
import com.example.localnotification.notification.remote.MockMessagingService
import com.example.localnotification.notification.remote.TopicRegistry

/**
 * アプリ全体で 1 度だけ呼ばれる初期化ポイント。
 *
 * **設計判断**:
 * - Application#onCreate で NotificationChannel を作成しておくと、最初の通知発行までに
 *   必ずチャンネルが存在することが保証できる (= 通知が黙って失敗するバグを防げる)。
 * - createNotificationChannel は同一 ID で重複呼び出しても no-op だが、
 *   ユーザーが OS 設定で変更した sound / importance はアプリ側から上書きできない。
 *   そのため「アプリ起動のたびに作り直す」のは性能・正当性ともに問題ない。
 * - Hilt 等の DI を導入していない学習用構成のため、依存はクラス変数として手動配線する。
 */
class LocalNotificationApp : Application() {

    /** Step 8 で使うトピック購読レジストリ。Application スコープで保持する。 */
    lateinit var topicRegistry: TopicRegistry
        private set

    /**
     * アプリプロセス起動時に 1 回だけ呼ばれる。
     * ここで NotificationChannel を登録しておくと、以降の notify() が確実に表示される。
     */
    override fun onCreate() {
        super.onCreate()
        NotificationChannelRegistrar.registerAll(this)

        // Step 8: FCM モックの配線。本物の FCM では AndroidManifest 登録だけで OS が起動するが、
        // ここでは object に Delegate を差し込むことで同等の挙動を再現する。
        MessagingMock.setDelegate(MockMessagingService())
        topicRegistry = TopicRegistry(this)
    }
}
