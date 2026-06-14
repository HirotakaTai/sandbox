# AGENTS.md

## このプロジェクトについて

**Agent Skills** をインストールして動作確認するための検証用プレイグラウンド。アプリケーションコードはなく、スキル定義（`SKILL.md`）と設定ファイルだけで構成される。

スキルは [Skills CLI](https://skills.sh/)（`npx skills`）で管理する。ビルドや依存関係のインストールは不要。

## 絶対に守るルール（最重要）

1. **スキルは必ずプロジェクトローカルにインストールする。`-g` フラグは絶対に付けない。**
2. **インストール後、必ず `.claude/skills/<skill-name>` のシンボリックリンクを作成・確認する。** これが無いと VS Code はスキルを認識しない。
3. **`skills-lock.json` を手動編集しない。** 必ず `npx skills` コマンドの実行結果として更新させる。
4. **信頼できない提供元のスキルはインストールしない。** インストール前に提供元・インストール数・スター数を確認する。
5. **スキルをインストール・削除したら、同じ作業の中で必ず `README.md` を更新する。** スキル一覧（分類・スキル名・提供元・概要）を `skills-lock.json` と一致させる。README.md の更新を別作業に先送りしない。

## ディレクトリ構成

```
.
├── .agents/skills/<skill-name>/SKILL.md   # スキルの実体（インストール先）
├── .claude/skills/<skill-name>            # → ../../.agents/skills/<skill-name> へのシンボリックリンク
├── .claude/settings.local.json            # `npx skills *` の実行許可
├── .vscode/settings.json                  # chat.agentSkillsLocations（.claude/skills のみ true）
└── skills-lock.json                       # インストール済みスキルのロックファイル
```

VS Code はスキルを **`.claude/skills` 経由でのみ**参照する。実体は `.agents/skills` に置き、`.claude/skills` からシンボリックリンクで繋ぐ。

## 作業手順

### 1. スキルを探す

`find-skills` スキルを使う（「〜するスキルはある?」「〜のやり方は?」といった要望からスキルを発見・推薦する）。

```bash
npx skills find <query>          # 例: npx skills find react performance
```

推薦・選定の前に必ず確認すること:
- インストール数（**1K+ を推奨**）
- 提供元の信頼性（`vercel-labs`、`anthropics`、`microsoft` などの公式ソースを優先）
- GitHub スター数

### 2. スキルをインストールする

```bash
# 必ずプロジェクトルートで実行。-g は付けない
npx skills add <owner/repo@skill>
```

実行すると実体が `.agents/skills/<skill-name>/` に展開され、`skills-lock.json` が更新される。

### 3. シンボリックリンクを作成・確認する

```bash
# リンクが無ければ作成（プロジェクトルートで実行）
ln -s ../../.agents/skills/<skill-name> .claude/skills/<skill-name>

# 確認: リンクが .agents/skills/<skill-name> を指していること
ls -la .claude/skills
```

### 4. README.md を更新する（インストール・削除のたびに必須）

スキルを **追加・削除したら、同じ作業の中で `README.md` のスキル一覧を更新する**。

- 追加したスキルを正しい分類（例: 「ドキュメント生成」「スキル管理」）の表に行追加する。削除したスキルは表から行削除する。
- 各行は **スキル名 / 提供元 / 概要** を記載する。スキル名・提供元は `skills-lock.json`、概要は `.agents/skills/<skill-name>/SKILL.md` のフロントマター `description` から正確に転記する。
- 最終的に、README.md のスキル一覧と `skills-lock.json` の `skills` キーが **過不足なく一致している**ことを確認する。

### 5. スキルを削除する

```bash
# プロジェクトルートで実行。skills-lock.json も更新される
npx skills remove <skill-name>

# シンボリックリンクが残っていれば削除する
rm .claude/skills/<skill-name>
ls -la .claude/skills   # 不要なリンクが残っていないことを確認
```

削除後は **手順 4 に従って `README.md` から該当スキルを除く**こと。

### 6. スキルを更新する

```bash
npx skills check    # 更新の有無を確認
npx skills update   # 更新を適用
```

更新でスキルの説明（`description`）が変わった場合は、`README.md` の概要欄も合わせて更新する。

## スキル定義（SKILL.md）の書き方

- Markdown で記述し、先頭に `name` と `description` を持つ YAML フロントマターを付ける。
- ディレクトリ名・スキル名は**ケバブケース**（例: `find-skills`、`create-readme`）。

## プルリクエスト

- タイトル形式: `[skills] <変更内容の要約>`
- スキルを追加・削除・更新したら、次の **4 点が整合している**ことを必ず確認する:
  1. `.agents/skills/<skill-name>/`（実体）
  2. `.claude/skills/<skill-name>`（シンボリックリンク）
  3. `skills-lock.json`（ロックファイル）
  4. `README.md`（スキル一覧。`skills-lock.json` と過不足なく一致していること）

## 補足

- VS Code 以外のエージェント環境では `chat.agentSkillsLocations` 相当の設定が無く、参照されるスキルディレクトリが異なる場合がある。
