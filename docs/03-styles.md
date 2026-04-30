# Step 3 — スタイル付き通知

折りたたまれた通知を **展開時に表現力豊かにする** スタイル群を学びます。

## 学ぶこと

| Style | 用途 | サンプル |
|-------|------|----------|
| `BigTextStyle` | 長文 (お知らせ、メッセージプレビュー) | Step 3 の最初のボタン |
| `BigPictureStyle` | 画像付き (写真、QR、図) | 2 番目のボタン |
| `InboxStyle` | 複数行リスト (未読リスト) | 3 番目のボタン |

ほかにも `MessagingStyle` (チャットアプリ向け)、`MediaStyle` (再生コントロール) がありますが、専門知識を要するため本サンプルでは扱いません。

## 実装ポイント

### BigTextStyle

```kotlin
val style = NotificationCompat.BigTextStyle()
    .setBigContentTitle("BigText のサンプル")
    .setSummaryText("展開すると長いテキストが表示されます")
    .bigText("これは BigTextStyle のサンプルです。\n通常は…")
NotificationCompat.Builder(context, channelId)
    .setStyle(style)
    .setContentText("折りたたみ時の本文")  // 重要: 展開前の表示
    .build()
```

### BigPictureStyle

`bigPicture(Bitmap)` に Bitmap を渡します。VectorDrawable は直接渡せないので Canvas 経由で Bitmap 化する必要があります。本サンプルでは [`buildBigPicture`](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt) で変換しています。

> 大きな画像 (>1MB) を渡すと OS によって拒否されます。サムネイルサイズに収めること。

### InboxStyle

```kotlin
NotificationCompat.InboxStyle()
    .setBigContentTitle("未読メッセージ 3 件")
    .addLine("田中: 明日の MTG ですが…")
    .addLine("鈴木: 議事録を共有しました。")
    .addLine("山田: 例の件、進捗どうですか?")
```

5〜7 行までしか表示されません。それ以上ある場合は `setSummaryText("他 N 件")` などで補足。

## 落とし穴 ⚠️

| 症状 | 原因 |
|------|------|
| 展開時の表示が反映されない | `setStyle` 呼び忘れ。または `setContentText` だけ書いて style 未設定 |
| BigPicture で OOM | Bitmap が巨大。`Bitmap.Config.ARGB_8888` で計算してメモリ占有量を見積もる |

## 関連ファイル

- [NotificationBuilders.buildBigText / buildBigPicture / buildInbox](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt)
- [Step3Screen.kt](../app/src/main/java/com/example/localnotification/ui/step3/Step3Screen.kt)
