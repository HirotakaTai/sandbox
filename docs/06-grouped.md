# Step 6 — グループ通知

複数の通知を **1 つのバンドルにまとめて表示** します。メッセンジャーアプリの "未読 N 件" 風 UX に必須。

## 学ぶこと

1. `setGroup(groupKey)` で同一グループに所属させる
2. `setGroupSummary(true)` でサマリー通知を作成
3. 子通知 → サマリーの **発行順** が重要

## 実装ポイント

### 子通知

```kotlin
NotificationCompat.Builder(context, channelId)
    .setSmallIcon(...)
    .setContentTitle(sender)
    .setContentText(message)
    .setGroup(GROUP_KEY_STEP6)  // ★ 全員同じ key
    .build()
```

### サマリー通知

```kotlin
NotificationCompat.Builder(context, channelId)
    .setSmallIcon(...)
    .setContentTitle("3 件の新着メッセージ")
    .setStyle(InboxStyle().addLine(...).addLine(...))
    .setGroup(GROUP_KEY_STEP6)
    .setGroupSummary(true)  // ★ 必須
    .build()
```

### 発行順

```kotlin
// 1) 子通知をすべて先に発行
sampleMessages.forEachIndexed { index, (sender, msg) ->
    poster.notify(CHILD_BASE + index, buildChild(sender, msg))
}
// 2) 最後にサマリーを発行
poster.notify(SUMMARY_ID, buildSummary())
```

逆順にするとデバイスによって自動グルーピングが期待通りに動かないことがあります (Pixel/Samsung で挙動差あり)。

## Android バージョンによる挙動差

| バージョン | 挙動 |
|-----------|------|
| Android 7.0+ (API 24) | 子通知 4 件以上で **自動的にバンドル表示**。サマリー通知は折りたたまれる |
| Android 6 以下 | サマリーのみ表示される (子は隠れる) |

minSdk 24 の本プロジェクトでは前者だけを考慮すれば OK です。

## 落とし穴 ⚠️

| 症状 | 対策 |
|------|------|
| グループ化されない | サマリーと子で `groupKey` が一致しているか確認 |
| サマリーが二重表示 | `setGroupSummary(true)` の通知が複数ある。グループあたり 1 件まで |
| 子をキャンセルしてもサマリーが残る | 個別に `cancel(SUMMARY_ID)` する。または `cancelGroupedNotifications()` 相当を自前実装 |

## 関連ファイル

- [NotificationIds.kt](../app/src/main/java/com/example/localnotification/notification/NotificationIds.kt)  (GROUP_KEY_STEP6)
- [NotificationBuilders.buildGroupedChild / buildGroupedSummary](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt)
- [Step6Screen.kt](../app/src/main/java/com/example/localnotification/ui/step6/Step6Screen.kt)
