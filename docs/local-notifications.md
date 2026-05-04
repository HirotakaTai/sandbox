# ローカル通知の実装メモ

このドキュメントは、サンプルコードを読む前後に確認するための短い解説です。

## ローカル通知の基本形

ローカル通知は、次の4ステップで予約します。

1. `UNMutableNotificationContent` で通知の見た目を作る
2. `UNNotificationTrigger` で通知される条件を作る
3. `UNNotificationRequest` で content と trigger をまとめる
4. `UNUserNotificationCenter.current().add(request)` でシステムに登録する

このプロジェクトでは、4ステップを `LocalNotificationManager.makeRequest(for:)` と `LocalNotificationManager.schedule(_:)` に分けています。

## 権限を先に確認する理由

通知の許可状態は、アプリを起動している間にも設定アプリから変更できます。そのため、予約ボタンを押すたびに `notificationSettings()` を呼び、現在の許可状態を確認しています。

許可状態が `.notDetermined` のときは、まだユーザーが選択していません。このサンプルでは、予約ボタンから自動で許可を求めず、明示的に `許可をリクエスト` ボタンを押す構成にしています。

## 予約IDを固定している理由

`UNNotificationRequest` には `identifier` があります。このサンプルでは、各パターンに固定の予約IDを使っています。

固定IDにすると、同じパターンを何度も予約したときに古い予約を置き換えられます。初学者が予約一覧を見たときに、同じ通知が大量に並ばないので、状態を理解しやすくなります。

## アプリが前面にあるとき

アプリが開いている状態で通知の時刻を迎えると、通知は `UNUserNotificationCenterDelegate` に渡されます。何もしないとバナー表示されないことがあるため、このサンプルでは `LocalNotificationForegroundDelegate` で `.banner`, `.list`, `.sound` を返しています。

delegate は `UNUserNotificationCenter` に弱参照で保持されます。サンプルでは `LocalNotificationManager` が delegate をプロパティとして持ち、途中で解放されないようにしています。

## 手動確認チェックリスト

- 初回起動後、`許可をリクエスト` でシステムの許可ダイアログが表示される
- 許可後、`5秒後に1回だけ` の通知が届く
- `次の1分ちょうどに1回だけ` の通知が予約一覧に表示される
- `毎日同じ時刻にくり返し` の通知が予約一覧に表示され、くり返しの説明が出る
- 予約一覧から1件だけ取り消せる
- `すべて取消` で予約一覧が空になる
- アプリを開いたまま通知時刻を迎えても、前面表示の通知が確認できる
- 通知を拒否した状態では、予約できない理由が画面に表示される