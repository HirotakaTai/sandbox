---
mode: 'agent'
description: 'Android のローカル通知機能を 1 セッションで安全に実装するための構造化プロンプト。Android 13+ の権限・チャンネル・PendingIntent ルールに準拠。'
tools: ['codebase', 'editFiles', 'search', 'usages', 'problems']
---

# Android Local Notification 実装プロンプト

このリポジトリにローカル通知機能を実装する。`.github/instructions/android-local-notification.instructions.md` を**正典**として扱い、逸脱しないこと。

## 入力 (ユーザーから受け取る)

- 通知のユースケース (例: 「ボタン押下で即時通知」「指定時刻に通知」「アクションボタン付き通知」)
- 通知のタイトル / 本文 (またはサンプル)
- タップ時の遷移先 (デフォルトは `MainActivity` を再表示)

不足があれば**実装前に必ず質問**する。

## 実行手順

1. **既存状況の把握**
   - `app/src/main/AndroidManifest.xml` を読み、`POST_NOTIFICATIONS` 権限の有無を確認
   - `app/build.gradle.kts` の `minSdk` / `targetSdk` を確認 (33+ を前提)
   - `gradle/libs.versions.toml` から既存依存 (compose, lifecycle 等) を確認
   - 既存 `Application` クラスの有無と、`MainActivity` の構成を確認
2. **設計の提示** (コードを書く前に)
   - 追加/変更するファイル一覧
   - 必要な権限・依存の差分
   - PendingIntent の flag 選定根拠 (IMMUTABLE/MUTABLE)
   - チャンネル ID と importance
3. **段階的な実装** (各ステップでビルドが通る単位)
   - **Step 1**: `AndroidManifest.xml` に `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` を追加
   - **Step 2**: `Application` を作成 (or 更新) して `NotificationChannel` を `onCreate` で生成
   - **Step 3**: 通知ビルド処理を `Notifier` のような薄いクラスに切り出し (テスト容易性)
   - **Step 4**: Compose UI でランタイム権限要求 + 「通知を送る」ボタンを実装
   - **Step 5**: `PendingIntent` を `FLAG_IMMUTABLE` 付きで構築し `setContentIntent` に渡す
   - **Step 6** (任意): アクションボタン、`BigTextStyle`、グルーピングを段階的に追加
4. **学習用コメント** を以下の箇所に必ず入れる:
   - `POST_NOTIFICATIONS` 権限が必要な理由 (Android 13+)
   - `Build.VERSION.SDK_INT` ガードの意義
   - `FLAG_IMMUTABLE` の必要性 (Android 12+)
   - trampoline を避ける理由
5. **検証**
   - `./gradlew assembleDebug` でビルド確認 (実行はユーザーに任せる)
   - 動作確認手順を箇条書きで提示
   - `adb shell dumpsys notification` での確認方法を案内

## アウトプット規約

- すべての文字列は `res/values/strings.xml` に定義し、ハードコード禁止
- Kotlin、Material 3、StateHoisting 準拠
- `viewModelScope` / `lifecycleScope` 以外で coroutine を起動しない
- diff は最小単位、変更ファイルごとにヘッダー付きで提示
- 最後に **「次に学ぶこと」** を 3 つ提案 (例: スタイル付き通知、スケジュール通知、フォアグラウンドサービス通知)

## 完了条件

- [ ] 実機/エミュレータで通知バナーが表示される
- [ ] タップで指定 Activity が起動し、二重起動しない
- [ ] 権限拒否時に rationale UI が出る
- [ ] チャンネル設定画面が OS で表示できる
- [ ] `./gradlew lint` で通知周りの警告ゼロ
