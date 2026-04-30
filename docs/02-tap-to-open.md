# Step 2 — タップで画面遷移

通知をタップしてアプリの特定画面を開きます。**Trampoline 禁止** と **PendingIntent の immutability** が最重要トピックです。

## 学ぶこと

1. **PendingIntent.getActivity** で Activity 直行
2. **FLAG_IMMUTABLE / FLAG_UPDATE_CURRENT** の組み合わせ
3. **Notification Trampoline 制限** (Android 12+)
4. **launchMode="singleTask"** + `onNewIntent` で既存スタック保持

## 実装ポイント

```kotlin
val intent = Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    putExtra(EXTRA_NAV_ROUTE, "step2")
    putExtra(EXTRA_PAYLOAD, payload)
}
val pi = PendingIntent.getActivity(
    context, requestCode, intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)
```

### なぜ FLAG_IMMUTABLE が必須か

Android 12 (API 31) 以降、`PendingIntent` の作成時に `FLAG_IMMUTABLE` か `FLAG_MUTABLE` の **どちらかを必ず明示** しないと `IllegalArgumentException` でクラッシュします。

- 既定で `FLAG_IMMUTABLE` を使う (third-party が intent を書き換えられない)
- Mutable が必要なのは `RemoteInput` (Step 4 参照) など限定ケースのみ

### なぜ Trampoline は禁止か

Android 12 から、**通知から起動された BroadcastReceiver/Service の中で Activity を起動することが禁止** されました (黙って起動失敗、log のみ)。

→ タップで画面を開きたいなら **`getActivity` で直行** する。`getBroadcast` を経由してから Activity を起動する古いパターンは使えません。

### MainActivity 側の deep-link 受け

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    pendingDeepLink = intent.getStringExtra(EXTRA_NAV_ROUTE)
}
```

`mutableStateOf` で保持し、`LaunchedEffect(deepLink)` で `navController.navigate(deepLink)`。1 回消費したら null に戻すことで、構成変更で再度 navigate されないようにします。

## 落とし穴 ⚠️

| 症状 | 対策 |
|------|------|
| クラッシュ "Targeting S+ requires that one of FLAG_IMMUTABLE or FLAG_MUTABLE be specified" | `FLAG_IMMUTABLE` を必ず付ける |
| 古い payload が残る | `FLAG_UPDATE_CURRENT` を付与 |
| タップしても画面が変わらない | `singleTask` でない場合、新しい Activity が常に積まれる。Navigation の deep-link 受信が動かない |

## 関連ファイル

- [NotificationBuilders.buildTappable](../app/src/main/java/com/example/localnotification/notification/NotificationBuilders.kt)
- [MainActivity.kt](../app/src/main/java/com/example/localnotification/MainActivity.kt)
- [AndroidManifest.xml](../app/src/main/AndroidManifest.xml)  (`launchMode="singleTask"`)
