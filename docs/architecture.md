# アーキテクチャ詳細

## ファイル構成

```
app/src/main/
├── AndroidManifest.xml                       # 権限宣言、Application/Activity/Receiver 登録
├── java/com/example/localnotification/
│   ├── LocalNotificationApp.kt               # Application — チャンネル登録のみ
│   ├── MainActivity.kt                       # Compose ホスト + 通知タップ deep-link 受け
│   ├── notification/                         # ★ 通知ドメイン層
│   │   ├── NotificationIds.kt                # チャンネル ID / 通知 ID / グループキー
│   │   ├── NotificationChannelRegistrar.kt   # 起動時に 4 チャンネルを作成
│   │   ├── NotificationPoster.kt             # notify/cancel の薄いラッパー (権限チェック内蔵)
│   │   ├── NotificationBuilders.kt           # Step 1〜7 の Notification 構築 (純粋関数)
│   │   ├── NotificationActionReceiver.kt     # Step 4: 既読/返信を Receiver で完結
│   │   ├── NotificationEvents.kt             # Receiver → UI のイベント SharedFlow
│   │   ├── WorkScheduler.kt                  # Step 7: WorkManager の薄いラッパー
│   │   └── NotificationWorker.kt             # Step 7: 実際に通知を発行する Worker
│   └── ui/
│       ├── AppNavHost.kt                     # Routes と NavHost
│       ├── theme/Theme.kt                    # Material 3 + Dynamic Color
│       ├── common/
│       │   ├── Permission.kt                 # POST_NOTIFICATIONS 権限ハンドラ
│       │   └── StepScaffold.kt               # 各 Step 共通 Scaffold (TopAppBar)
│       ├── home/HomeScreen.kt                # Step リスト
│       ├── step1/Step1Screen.kt              # ... 各 Step の Composable
│       └── ...
└── res/                                      # 文字列、ベクター、テーマ
```

## 依存方向

依存は **UI → notification → OS API** の一方向で、逆向きの依存はありません。

```
ui/* ──depends on──► notification/* ──depends on──► android.app.Notification* / NotificationManagerCompat
                                  │
                                  └──no dependency on──► ui/*
```

これにより:
- 通知ロジック (`notification/`) を UI と独立に unit test できる
- 画面追加時に通知層を変更する必要が無い

## 主要な設計判断

### 1. 単一 Activity + Compose Navigation

複数 Activity 構成を採用しなかった理由:
- 通知タップで開く画面を増やすと Activity が増殖する
- `singleTask` × `Intent.flags` のハマりを避ける
- Navigation の back stack が読みやすい

`MainActivity.launchMode = "singleTask"` + `onNewIntent` で deep-link 用の route 文字列を `mutableStateOf` に流し、`LaunchedEffect` で `navController.navigate()` を呼ぶシンプルな実装にしています。

### 2. 通知ヘルパーを `object` で集約

依存性注入 (Hilt 等) を入れないことで、学習のノイズを減らしています。代わりに:
- `NotificationBuilders` は **純粋関数** (`Context` を引数にとり `Notification` を返す)。テスタブル
- `NotificationPoster` だけを軽量クラスにして、`Context` を保持

### 3. Receiver → UI のイベント伝達は SharedFlow

`NotificationEvents.events: SharedFlow<Event>` をプロセス内 singleton (object) として置いています。

理由:
- BroadcastReceiver は寿命が短い (≈10 秒) ため、`LiveData` のような observer 登録は適さない
- Compose 側で `LaunchedEffect { events.collect { ... } }` するのが最も自然
- バッファ 16 + DROP_OLDEST で UI 不在時の暴走を防止

### 4. 進捗通知のループは画面側 LaunchedEffect

WorkManager まで動員すると "通知 API の学習" から外れるため、Step 5 は `LaunchedEffect(running) { while(...) { delay(500); poster.notify(...) } }` で実装。`running` を切り替えれば自動キャンセル。

### 5. 予約通知は WorkManager 一本

`AlarmManager` を選ばなかった理由:
- Android 14+ で `SCHEDULE_EXACT_ALARM` がユーザー確認必須化 → UX 悪化
- WorkManager は再起動跨ぎ + Doze 協調を OS 側でやってくれる
- 学習用には十分な精度 (秒オーダーのずれは許容)

正確な時刻指定 (例: アラームクロック) が必要な場合は別途 `setExactAndAllowWhileIdle` を学ぶ必要がありますが、本サンプルではスコープ外です。

## 通知 ID の命名規約

[NotificationIds.kt](../app/src/main/java/com/example/localnotification/notification/NotificationIds.kt) では:
- 1xxx 番台は通知 ID
- Step ごとに 10 番ずつ予約 (1001=Step1, 1011〜1013=Step3 の 3 種, 1101〜=Step6 の子要素ベース, 1200=Step7)
- グループキーは文字列で別管理

通知 ID を散在させると更新/取消の対象を見失うため、**1 ファイルに集約** することを強く推奨します。

## チャンネル設計

| Channel ID | importance | 用途 |
|------------|-----------|------|
| `basic` | DEFAULT | Step 1, 2, 3, 5 |
| `actions` | HIGH (heads-up) | Step 4 (返信) |
| `grouped` | DEFAULT | Step 6 |
| `scheduled` | DEFAULT | Step 7 |

> **重要**: チャンネル ID は **公開後に変更してはいけません**。ユーザーが個別にカスタマイズした設定 (音、バイブ、優先度) が引き継がれず、新しいチャンネルとして再表示されてしまうためです。挙動を変えたい場合は新しい ID を作り、旧チャンネルを `deleteNotificationChannel` で消すのが定石です。
