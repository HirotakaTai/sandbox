# iOS Local Notification Sample

SwiftUI と `UserNotifications` を使って、iOS のローカル通知を学ぶための初学者向けサンプルです。

このプロジェクトでは、アプリを起動してすぐに次の流れを試せます。

1. 通知の許可をリクエストする
2. ローカル通知を予約する
3. 予約中の通知を一覧で見る
4. 予約した通知を取り消す
5. アプリが前面にあるときの通知表示を確認する

## 実装している通知パターン

| パターン | 使うAPI | 学べること |
| --- | --- | --- |
| 5秒後に1回だけ | `UNTimeIntervalNotificationTrigger` | 現在時刻からの相対時間で通知する方法 |
| 次の1分ちょうどに1回だけ | `UNCalendarNotificationTrigger` | 年月日と時刻を指定して通知する方法 |
| 毎日同じ時刻にくり返し | `UNCalendarNotificationTrigger` + `repeats` | 時刻だけを指定して定期通知にする方法 |

## 主要ファイル

| ファイル | 役割 |
| --- | --- |
| `ios-local-notification/ContentView.swift` | サンプル操作用の SwiftUI 画面 |
| `ios-local-notification/LocalNotificationManager.swift` | 権限確認、通知予約、一覧取得、キャンセル、前面表示 delegate |
| `ios-local-notification/NotificationSamplePattern.swift` | 通知パターンと予約中通知の表示用モデル |
| `ios-local-notification/ios_local_notificationApp.swift` | アプリ起動時に通知マネージャーを画面へ渡す |

## 動かし方

1. Xcode で `ios-local-notification.xcodeproj` を開きます。
2. iPhone Simulator または実機を選びます。
3. アプリを実行します。
4. `許可をリクエスト` を押し、通知を許可します。
5. 予約パターンを選び、通知が届くことを確認します。

通知の許可ダイアログは、同じアプリの同じインストールでは基本的に1回だけ表示されます。初回許可の動きをもう一度確認したい場合は、Simulator または実機からアプリを削除してから再実行してください。

## コマンドラインでのビルド確認

フル Xcode が選択されている環境では、次のようにビルドできます。

```sh
xcodebuild -project ios-local-notification.xcodeproj \
  -scheme ios-local-notification \
  -destination 'platform=iOS Simulator,name=<available iPhone simulator>' \
  build
```

`xcodebuild` が `/Library/Developer/CommandLineTools` を指している場合は、Command Line Tools だけが有効になっています。コマンドラインビルドを使うには、フル Xcode を選択してください。

## さらに読む

- [ローカル通知の実装メモ](docs/local-notifications.md)
- Apple: Scheduling a notification locally from your app  
  https://developer.apple.com/documentation/usernotifications/scheduling-a-notification-locally-from-your-app
- Apple: Asking permission to use notifications  
  https://developer.apple.com/documentation/usernotifications/asking-permission-to-use-notifications