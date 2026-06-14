# Skills Playground

**Agent Skills** をインストールして動作確認するための検証用プレイグラウンドです。アプリケーションコードは持たず、スキル定義（`SKILL.md`）と設定ファイルだけで構成されています。

スキルは [Skills CLI](https://skills.sh/)（`npx skills`）で管理します。ビルドや依存関係のインストールは不要で、追加したスキルは VS Code / Claude Code などのエージェント環境からそのまま利用できます。

> [!NOTE]
> このリポジトリは「スキルを試す場所」です。新しいスキルを見つけて入れ、挙動を確かめ、不要になれば外す——その一連の流れを安全に回すことを目的としています。

## 主な特徴

- **設定のみで完結** — アプリケーションコードや実行環境のセットアップが不要。
- **プロジェクトローカル管理** — スキルはグローバルではなくリポジトリ内にインストールし、`skills-lock.json` でバージョンを固定。
- **VS Code 連携** — `.claude/skills` へのシンボリックリンク経由でエディタがスキルを認識。

## インストール済みスキル

スキルの実体は `.agents/skills/<skill-name>/SKILL.md` に置かれ、`.claude/skills/<skill-name>` からのシンボリックリンクで参照されます。

### ドキュメント生成

| スキル | 提供元 | 概要 |
| --- | --- | --- |
| `create-readme` | `github/awesome-copilot` | プロジェクト全体を読み取り、構成の整った魅力的な README.md を生成する。 |
| `create-agentsmd` | `github/awesome-copilot` | [agents.md](https://agents.md/) の公式ガイドに沿って、コーディングエージェント向けの AGENTS.md を生成する。 |
| `documentation-writer` | `github/awesome-copilot` | [Diátaxis フレームワーク](https://diataxis.fr/)（チュートリアル / ハウツー / リファレンス / 解説）に基づいて高品質な技術ドキュメントを作成する。 |

### ドキュメント参照

| スキル | 提供元 | 概要 |
| --- | --- | --- |
| `context7-cli` | `upstash/context7` | `ctx7` CLI を使って、任意のライブラリの最新ドキュメント取得、AI コーディングスキルの管理、Context7 MCP のセットアップを行う。 |

### プランニング・設計

| スキル | 提供元 | 概要 |
| --- | --- | --- |
| `grill-me` | `mattpocock/skills` | プラン・デザインについて徹底的にインタビューして、意思決定の各ブランチを解決し共通理解に達する。ストレステストや検証に最適。 |
| `grill-with-docs` | `mattpocock/skills` | グリリングセッション中にプランを既存ドメインモデルに対して検証し、用語をシャープにし、CONTEXT.md や ADR を更新する。 |

### スキル管理

| スキル | 提供元 | 概要 |
| --- | --- | --- |
| `find-skills` | `vercel-labs/skills` | 「〜するスキルはある?」「〜のやり方は?」といった要望から、オープンなエコシステムのスキルを探して推薦・インストールする。 |

## ディレクトリ構成

```
.
├── README.md                              # このプロジェクトの概要（人間向けドキュメント）
├── AGENTS.md                              # コーディングエージェント向けの指示と規約
├── skills-lock.json                       # インストール済みスキルのロックファイル
├── .agents/
│   └── skills/<skill-name>/SKILL.md       # スキルの実体（インストール先）
├── .claude/
│   ├── settings.local.json                # `npx skills *` の実行許可
│   └── skills/<skill-name>                # → ../../.agents/skills/<skill-name> へのシンボリックリンク
└── .vscode/
    └── settings.json                      # chat.agentSkillsLocations（.claude/skills のみ true）
```

### ファイルの役割

- **README.md** — このプロジェクトの概要、使い方、運用ルールなど、プロジェクト参画者全員が最初に読むべき情報。
- **AGENTS.md** — コーディングエージェント（Copilot、Claude Code など）向けの技術指示。`skills-lock.json` との整合確認、PR 規約、スキル管理ワークフロー、シンボリックリンク作成などの詳細手順を記載。
- **CLAUDE.md** — Claude系エージェント向けの簡潔な参照。`@AGENTS.md` へのリンク。
- **skills-lock.json** — `npx skills` で自動生成・更新されるロックファイル。インストール済みスキル、ソース、ハッシュを記録。

VS Code はスキルを **`.claude/skills` 経由でのみ**参照します。実体は `.agents/skills` に置き、`.claude/skills` からシンボリックリンクで繋ぎます。

## 使い方

### スキルを探す

```bash
npx skills find <query>          # 例: npx skills find react performance
```

推薦・選定の前に、提供元の信頼性（`vercel-labs`、`anthropics`、`microsoft` などの公式ソースを優先）、インストール数（**1K+ 推奨**）、GitHub スター数を確認してください。

### スキルをインストールする

```bash
# 必ずプロジェクトルートで実行。-g は付けない
npx skills add <owner/repo@skill>
```

実行すると実体が `.agents/skills/<skill-name>/` に展開され、`skills-lock.json` が更新されます。

> [!IMPORTANT]
> インストール後は必ず `.claude/skills/<skill-name>` のシンボリックリンクを作成・確認してください。これが無いと VS Code はスキルを認識しません。
>
> ```bash
> ln -s ../../.agents/skills/<skill-name> .claude/skills/<skill-name>
> ls -la .claude/skills   # リンクが .agents/skills/<skill-name> を指していることを確認
> ```

### スキルを更新する

```bash
npx skills check    # 更新の有無を確認
npx skills update   # 更新を適用
```

## 運用ルール

> [!WARNING]
> 以下は本プレイグラウンドを安全に運用するための必須ルールです。

1. **スキルは必ずプロジェクトローカルにインストールする。`-g` フラグは絶対に付けない。**
2. **インストール後、必ず `.claude/skills/<skill-name>` のシンボリックリンクを作成・確認する。**
3. **`skills-lock.json` を手動編集しない。** 必ず `npx skills` コマンドの実行結果として更新させる。
4. **信頼できない提供元のスキルはインストールしない。** インストール前に提供元・インストール数・スター数を確認する。

スキルを追加・更新した際は、次の **3 点が整合している**ことを確認してください。

1. `.agents/skills/<skill-name>/`（実体）
2. `.claude/skills/<skill-name>`（シンボリックリンク）
3. `skills-lock.json`（ロックファイル）

詳細な作業手順とエージェント向けの指示は [AGENTS.md](AGENTS.md) を参照してください。
