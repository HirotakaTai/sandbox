# Workspace Copilot Instructions

このワークスペースは **Android のローカル通知 (Local Notification) を学習するためのサンプルアプリ** です。
コーディングエージェントは以下のガイドラインに従って支援を行ってください。

## プロジェクト概要

- **目的**: Android の `NotificationManagerCompat` / `NotificationCompat.Builder` / `NotificationChannel` を用いた
  ローカル通知の実装パターンを学習する。
- **言語**: Kotlin (公式推奨)
- **ビルド**: Gradle Kotlin DSL (`build.gradle.kts`)、依存は `gradle/libs.versions.toml` で集中管理 (Version Catalog)。
- **最低 / ターゲット SDK**: `app/build.gradle.kts` を参照。新規 API を提案する際は **API 33 (Android 13) 以降** を前提とし、
  必ず `Build.VERSION.SDK_INT` ガードまたは `@RequiresApi` を付ける。
- **UI**: Jetpack Compose Material 3 を優先。
- **アーキテクチャ**: 学習用なので過剰な抽象化はしない。`MainActivity` + 単一 `ViewModel` + 純粋関数 helper で十分。

## エージェント全般の振る舞い

1. 推測ではなくコードを読んで答える。`AndroidManifest.xml`、`build.gradle.kts`、`libs.versions.toml` は最初に確認する。
2. 学習目的のため、**「なぜそうするのか」をコメントで説明** する。
   ただし冗長な docstring は避け、API 互換や権限まわりなど "ハマりどころ" にコメントを集中する。
3. 破壊的変更 (権限追加、targetSdk 変更、依存追加) は **必ず差分を提示してから** 適用する。
4. ローカル通知の実装は `instructions/android-local-notification.instructions.md` に厳密に従う。
5. Kotlin / Compose のスタイルは `instructions/android-kotlin.instructions.md` と
   `instructions/android-jetpack-compose.instructions.md` に従う。

## 参照すべきカスタマイズファイル

| 種別 | パス | 用途 |
|------|------|------|
| Instructions | [.github/instructions/android-local-notification.instructions.md](.github/instructions/android-local-notification.instructions.md) | 通知実装の必須ルール |
| Instructions | [.github/instructions/android-kotlin.instructions.md](.github/instructions/android-kotlin.instructions.md) | Kotlin / Android 共通規約 |
| Instructions | [.github/instructions/android-jetpack-compose.instructions.md](.github/instructions/android-jetpack-compose.instructions.md) | Compose 固有規約 |
| Instructions | [.github/instructions/security-and-owasp.instructions.md](.github/instructions/security-and-owasp.instructions.md) | セキュリティ全般 |
| Instructions | [.github/instructions/self-explanatory-code-commenting.instructions.md](.github/instructions/self-explanatory-code-commenting.instructions.md) | コメント方針 |
| Agent | [.github/agents/android-notification-expert.agent.md](.github/agents/android-notification-expert.agent.md) | 通知実装の専門エージェント |
| Prompt | [.github/prompts/android-notification-implementation.prompt.md](.github/prompts/android-notification-implementation.prompt.md) | 通知機能を 1 セッションで実装する手順 |

## コミット規約

- Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:` 等) を使用。
- 日本語コミットメッセージ可。詳細は [.github/skills/conventional-commit/SKILL.md](.github/skills/conventional-commit/SKILL.md) を参照。
