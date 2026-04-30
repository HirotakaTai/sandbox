---
applyTo: '**/*.kt,**/*.kts'
description: 'Kotlin と Android (API 33+) で守るべきコーディング規約。学習用プロジェクト向け。'
---

# Android / Kotlin Coding Standards

このリポジトリ全体の `.kt` / `.kts` ファイルに適用されます。

## Kotlin 言語

- **Null 安全**: Platform 型 (`String!`) を意識し、外部 API 由来の値は早期に `?` または `!!` ではなく `requireNotNull()` /
  `checkNotNull()` で扱う。`!!` は **テストコード以外では使わない**。
- **不変性優先**: `val` / `List` / `Map` をデフォルトに。可変が必要なときだけ `var` / `MutableList`。
- **データクラス**: イミュータブルな値オブジェクトには `data class`。`copy()` で派生を作る。
- **Sealed class / interface** で状態 (UI State, Result) を表現する。`when` 式は exhaustive にする。
- **拡張関数** はユーティリティに留め、ドメインロジックは普通のクラスメンバに置く。
- **Coroutines**:
  - UI 起点は `viewModelScope` または `lifecycleScope` を使用。`GlobalScope` 禁止。
  - I/O は `withContext(Dispatchers.IO)`。CPU 重処理は `Dispatchers.Default`。
  - 例外は `try/catch` で握り潰さず、`CoroutineExceptionHandler` か上位で `Result` 型として伝播。
- **Flow**: cold stream には `Flow`、UI へは `StateFlow` / `SharedFlow` で公開。`LiveData` は新規採用しない。

## Android 固有

- **最小バージョンチェック**: `Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU` のように比較し、
  ブロックには `@RequiresApi(Build.VERSION_CODES.TIRAMISU)` を付ける。
- **権限**: dangerous permission (POST_NOTIFICATIONS, RECORD_AUDIO 等) は **必ず Activity Result API**
  (`registerForActivityResult(ActivityResultContracts.RequestPermission())`) でリクエスト。
  `ActivityCompat.requestPermissions` は新規採用しない。
- **Context**: 長期保持が必要な場合は `applicationContext` を使用。`Activity` への参照を ViewModel / Singleton に保持しない (リーク)。
- **Intent**:
  - 暗黙 Intent は最小限。明示 Intent には `setPackage()` か `Component` を指定。
  - `PendingIntent` には Android 12 (API 31) 以降 `FLAG_IMMUTABLE` または `FLAG_MUTABLE` を **必須** で指定。
  - 通知/アラーム経由の起動には **trampoline activity を使わない** (Android 12+ で禁止)。
- **WorkManager**: 即時かつ短時間処理は coroutine で完結させる。永続化が必要なバックグラウンド処理だけ WorkManager を使う。
- **DI**: 学習用プロジェクトでは Hilt は導入しない。手動 DI (Application で生成、ViewModelFactory で渡す) を選ぶ。
- **リソース**:
  - 文字列は必ず `strings.xml` に定義 (i18n)。
  - 色は `colorResource` / Material Theme から取得し、ハードコード禁止。
  - drawable は SVG ベクター優先。

## ビルド設定

- 依存追加は `gradle/libs.versions.toml` に記述し、`build.gradle.kts` から `libs.foo.bar` で参照する。
- バージョンを直書き (`implementation("group:artifact:1.2.3")`) しない。
- `kotlinOptions { jvmTarget = "17" }` または相当を維持。

## テスト

- 単体テストは `app/src/test` に JUnit5 (Jupiter) で記述。
- Android 依存のテストは `app/src/androidTest` に Espresso / Compose UI Test。
- 通知ロジックは Android 依存部 (`NotificationManagerCompat`) を薄いインターフェースで包んで unit testable にする。

## 禁止事項

- `runBlocking` を本番コードで使用しない (テストのみ)。
- `Thread.sleep` を本番コードで使用しない。
- `findViewById` を新規追加しない (Compose または ViewBinding を使用)。
- `AsyncTask`、`Loader`、`Handler(Looper.getMainLooper())` の新規採用禁止。
