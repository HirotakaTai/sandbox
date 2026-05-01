# Step 8 — リモートプッシュ通知 (FCM モック)

Firebase Cloud Messaging (FCM) の **構造** を、Firebase 依存ゼロで学習するための擬似実装です。本物の `FirebaseMessagingService` / `RemoteMessage` / `FirebaseMessaging.getToken()` / `subscribeToTopic()` と同じシグネチャを自前で再現し、UI から「擬似的にメッセージを発火」させて受信処理を体験できます。

> ⚠️ **このサンプルは実際にネットワーク経由でプッシュを受信しません**。
> Firebase コンソールで実プロジェクトを構築するハードルを避けつつ、コード構造と通知発行/Deep Link/トピック購読/トークン管理の挙動を学ぶことが目的です。

## 学ぶこと

1. **FCM のメッセージ構造** — 「通知メッセージ」と「データメッセージ」の違い
2. **Service コールバックの責務** — `onMessageReceived` / `onNewToken` で何をすべきか
3. **トークン管理** — 取得 → サーバー登録 → ローテーション (`onNewToken`)
4. **トピック購読** — クライアント完結で永続化される購読モデル
5. **Deep Link** — `data: { route: ... }` で特定画面に遷移させる慣習
6. **フォアグラウンド受信時の通知発行** — 自前で `NotificationCompat.Builder` を組み立てる必要性

## 本物の FCM とのマッピング

| 本物 (Firebase SDK) | 本サンプル |
|---------------------|-----------|
| `class MyService : FirebaseMessagingService()` | `class MockMessagingService : MessagingDelegate()` |
| `RemoteMessage` | [RemoteMessage.kt](../app/src/main/java/com/example/localnotification/notification/remote/RemoteMessage.kt) |
| `FirebaseMessaging.getInstance().token` | [MessagingMock.token](../app/src/main/java/com/example/localnotification/notification/remote/MessagingMock.kt) |
| `FirebaseMessaging.getInstance().subscribeToTopic("news")` | `TopicRegistry.subscribe("news")` |
| Manifest `<service android:name=".MyService">` | `MessagingMock.setDelegate(MockMessagingService())` (Application#onCreate) |
| サーバーから push (HTTP v1 API) | UI ボタンの `MessagingMock.simulateMessage(...)` |

## 実装ポイント

### メッセージモデル (FCM ペイロードと同じ構造)

```kotlin
data class RemoteMessage(
    val notification: Notification? = null,   // タイトル/本文
    val data: Map<String, String> = emptyMap(), // 任意キー (deep link など)
    val messageId: String = "msg-${System.currentTimeMillis()}",
)
```

FCM の HTTP v1 ペイロードに対応:

```json
{
  "message": {
    "notification": { "title": "...", "body": "..." },
    "data": { "route": "step2", "payload": "..." }
  }
}
```

### Service 相当の Delegate

```kotlin
abstract class MessagingDelegate {
    abstract fun onMessageReceived(context: Context, message: RemoteMessage)
    abstract fun onNewToken(context: Context, token: String)
}
```

本物の `FirebaseMessagingService` 同様、メインスレッドかつ約 20 秒以内に処理を終わらせる必要があります (重い処理は WorkManager に逃がす)。

### 受信ハンドラ

```kotlin
class MockMessagingService : MessagingDelegate() {
    override fun onMessageReceived(context: Context, message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "通知"
        val body  = message.notification?.body  ?: message.data["body"]  ?: "..."
        val notif = NotificationBuilders.buildRemoteNotification(
            context, notifId, title, body,
            deepLinkRoute = message.deepLinkRoute,
        )
        NotificationPoster(context).notify(notifId, notif)
    }

    override fun onNewToken(context: Context, token: String) {
        scope.launch { MockServerApi.registerToken(token) }
    }
}
```

### Deep Link を埋め込む通知

```kotlin
val intent = Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    putExtra(EXTRA_NAV_ROUTE, deepLinkRoute)
}
val pi = PendingIntent.getActivity(
    context, notifId, intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)
```

`MainActivity#onNewIntent` で `EXTRA_NAV_ROUTE` を読み取って `navController.navigate(route)` する仕組みは [Step 2](02-tap-to-open.md) で実装済みのものをそのまま流用しています。

### トークンのローテーション

```kotlin
fun rotateToken(context: Context) {
    val newToken = generateToken()
    _token.value = newToken
    delegate?.onNewToken(context, newToken)  // ここで自社サーバーへ送信
}
```

本物の FCM では「初回起動時 / アプリデータ削除 / 再インストール / インスタンス ID プロバイダ更新時」に自動発火します。

### トピック購読

`TopicRegistry` は SharedPreferences に購読中トピックを保存するシンプルな実装です。本物の FCM でも「購読状態は端末側 + Firebase バックエンドが永続化」されるので、再起動後も維持されるという挙動はそのまま再現できます。

## 通知メッセージ vs データメッセージ

これは FCM 学習で **最も誤解されやすい部分** です。

| 種類 | アプリ前面 | アプリ背面 / 終了 | 通知バー表示 | onMessageReceived |
|------|-----------|-----------------|------------|------------------|
| 通知メッセージ (`notification`) | アプリが受信 | **OS が自動表示** | 自動 | 前面時のみ |
| データメッセージ (`data` のみ) | アプリが受信 | **アプリが起動して受信** | アプリが手動発行 | 常に呼ばれる |
| 両方 (`notification` + `data`) | アプリが受信 | OS が自動表示 + アプリ起動 | 自動 | 前面時のみ |

→ **deep link や独自処理が必要なら必ず「データメッセージのみ」で送信** するのが鉄則です (本サンプルの "データメッセージを擬似受信" ボタンが該当)。

## 落とし穴 ⚠️

| 症状 | 原因と対策 |
|------|-----------|
| 通知が表示されない | `POST_NOTIFICATIONS` 権限未許可。Step 8 画面上部の権限カードを確認 |
| トークンが毎回変わる | 本サンプルは UUID 生成 + StateFlow 保持のみ (永続化なし)。本物の FCM は端末固有 ID 由来で再起動跨ぎで維持される |
| Deep link で画面が開かない | `MainActivity#onNewIntent` の `EXTRA_NAV_ROUTE` 処理 (Step 2 参照) を確認 |
| サーバー登録が失敗しても気付けない | 本物では WorkManager の `Result.retry()` でリトライ機構を必ず用意する |
| 通知メッセージなのに前面時しか出ない | これは仕様。背面時 OS が表示するのは確認しづらいので、**学習中は常にデータメッセージで試す** のがおすすめ |

## 本物の FCM に置き換えるときの差分

このサンプルから本物の FCM に切り替えるときに必要な作業:

1. `build.gradle.kts` に Firebase BoM と `firebase-messaging-ktx` を追加
2. `google-services.json` を `app/` 直下に配置 + `com.google.gms.google-services` プラグイン適用
3. [MockMessagingService](../app/src/main/java/com/example/localnotification/notification/remote/MockMessagingService.kt) を `FirebaseMessagingService` を継承するように変更し、AndroidManifest.xml に Service 登録 (`com.google.firebase.MESSAGING_EVENT` フィルタ)
4. [MessagingMock](../app/src/main/java/com/example/localnotification/notification/remote/MessagingMock.kt) を捨てて `FirebaseMessaging.getInstance().token` / `subscribeToTopic()` を直接呼ぶ
5. `MockServerApi.registerToken` を本物の HTTPS POST に差し替え (Retrofit など)

→ **Domain 層の `RemoteMessage` / `MessagingDelegate` の型定義はそのまま流用可能** という設計になっています (Firebase の同名クラスにアダプトしやすい)。

## 関連ファイル

- [RemoteMessage.kt](../app/src/main/java/com/example/localnotification/notification/remote/RemoteMessage.kt) — メッセージモデル
- [MessagingDelegate.kt](../app/src/main/java/com/example/localnotification/notification/remote/MessagingDelegate.kt) — Service 相当の抽象
- [MockMessagingService.kt](../app/src/main/java/com/example/localnotification/notification/remote/MockMessagingService.kt) — Delegate 実装 (通知発行)
- [MessagingMock.kt](../app/src/main/java/com/example/localnotification/notification/remote/MessagingMock.kt) — `FirebaseMessaging` 相当の object
- [MockServerApi.kt](../app/src/main/java/com/example/localnotification/notification/remote/MockServerApi.kt) — `onNewToken` 時のサーバー登録擬似 API
- [TopicRegistry.kt](../app/src/main/java/com/example/localnotification/notification/remote/TopicRegistry.kt) — トピック購読の永続化
- [NotificationBuilders.buildRemoteNotification](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt) — リモート通知のビルド (deep link 対応)
- [Step8Screen.kt](../app/src/main/java/com/example/localnotification/ui/step8/Step8Screen.kt) — UI
- [LocalNotificationApp.kt](../app/src/main/java/com/example/localnotification/LocalNotificationApp.kt) — Delegate 配線

## 参考リンク

- [Firebase Cloud Messaging — Notification messages vs Data messages](https://firebase.google.com/docs/cloud-messaging/concept-options)
- [FirebaseMessagingService](https://firebase.google.com/docs/reference/android/com/google/firebase/messaging/FirebaseMessagingService)
- [FCM HTTP v1 API リファレンス](https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages)
