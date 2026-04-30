---
applyTo: '**/*.kt'
description: 'Jetpack Compose (Material 3) のコーディング規約。状態管理、副作用、再コンポジション最適化のベストプラクティス。'
---

# Jetpack Compose Standards

## 全般

- **Material 3** (`androidx.compose.material3`) を使用。Material 2 は新規コードで使わない。
- テーマは `MaterialTheme { ... }` でラップし、色は `MaterialTheme.colorScheme.*`、タイポは `MaterialTheme.typography.*` から取得。
- リソースアクセスは `stringResource(R.string.foo)` / `painterResource(R.drawable.bar)`。

## Composable 関数の書き方

```kotlin
@Composable
fun NotificationButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(stringResource(R.string.send_notification))
    }
}
```

- パラメータ順: 必須 → optional → `modifier: Modifier = Modifier` (常に最後に近い位置) → trailing lambda。
- **`modifier` パラメータを必ず公開**し、内部では `modifier.then(...)` ではなく `modifier` を素直に渡す。
- `Modifier` は新規生成ではなく**呼び出し側で組み立てる**。
- 名前は PascalCase、戻り値 Unit (UI を返さないものは `@Composable` にしない)。
- preview には `@Preview(showBackground = true)` を付け、`ThemedPreview` ラッパーを用意するとよい。

## State Hoisting

```kotlin
// BAD: 状態を内部に隠すと再利用できない
@Composable
fun BadCounter() {
    var count by remember { mutableIntStateOf(0) }
    Button(onClick = { count++ }) { Text("$count") }
}

// GOOD: 値とコールバックを引数化
@Composable
fun GoodCounter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) { Text("$count") }
}
```

- 親が状態を保持し、子は値とコールバックを受け取る。
- ViewModel から `StateFlow` を `collectAsStateWithLifecycle()` で受ける (lifecycle-aware)。

## 副作用 (Side Effect) API

| API | 用途 |
|-----|------|
| `LaunchedEffect(key)` | key 変化時に suspend 処理を起動 |
| `DisposableEffect(key)` | リソースの確保/解放 (Listener 登録など) |
| `SideEffect` | 毎回のコンポジション後に非 Compose 世界に同期 |
| `rememberCoroutineScope()` | クリックハンドラ等で coroutine を起動 |
| `produceState` | 外部ソースを State に変換 |
| `derivedStateOf` | 派生値を再計算最小化したい時 |

- 通知の権限ランチャーは `rememberLauncherForActivityResult` で取得。
- `LaunchedEffect(Unit)` は「最初の composition で 1 度だけ」の意味で使う。

## 再コンポジション最適化

- `mutableStateOf<Int>` ではなく `mutableIntStateOf` (boxing 回避)。
- `Modifier` チェーンは composable 内で再構築されないように `remember` または変数化。
- 大きなリストは `LazyColumn` + `key = { it.id }` を必ず指定。
- `@Stable` / `@Immutable` を必要に応じて自作データクラスに付与。
- `lambda` を引数で渡す時は親の状態を直接読み取らず関数参照や `rememberUpdatedState` を活用。

## ナビゲーション

- 学習用なので **Navigation Compose は導入しない**。`when (screen)` で切り替えで十分。
- 必要になったら `androidx.navigation:navigation-compose` を `libs.versions.toml` に追加してから提案する。

## テスト

- `androidx.compose.ui:ui-test-junit4` を使用。
- `composeTestRule.setContent { MyScreen() }` の後 `onNodeWithText("...")` で検証。
- `ComposeTestRule.mainClock.advanceTimeBy()` で待機制御。

## 禁止事項

- View システム (`AndroidView`) の濫用。やむを得ない場合のみ。
- `remember { mutableStateOf(...) }` を Composable 引数に直接渡す (ホイスティング崩壊)。
- `GlobalScope.launch` を Composable 内で使用。
