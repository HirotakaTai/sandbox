# Three.js 学習サンプル

Vite + TypeScript + Three.js + Biome で構築した、Three.js の基本機能を 1 つずつ確認できる学習者向けサンプルブラウザです。

## 開発

```bash
pnpm install
pnpm dev          # 開発サーバ
pnpm typecheck    # TypeScript 型チェック
pnpm check        # Biome lint + format チェック
pnpm build        # 本番ビルド
```

## サンプル一覧

| ID | 内容 | 難易度 |
| --- | --- | --- |
| `rotating-cube` | Scene / Camera / Mesh の最小構成で立方体を回転 | 初級 |
| `lighting` | Ambient / Directional / Point ライトの比較 | 初級 |
| `texture` | CanvasTexture によるプロシージャル UV マッピング | 中級 |
| `controls` | OrbitControls によるカメラ操作 | 中級 |
| `postprocessing` | EffectComposer + UnrealBloomPass による発光表現 | 上級 |

各サンプルの詳細は [`docs/samples/`](./docs/samples/) を参照してください。

## アーキテクチャ概要

```
src/
  app/             サイドバー / ヘッダ / ハッシュルータ（UI シェル）
  core/            サンプルランタイム（共有 WebGLRenderer / アニメーションループ）
  samples/<id>/    各サンプル（init / render / dispose ライフサイクル）
  styles/          アプリ全体の CSS
  main.ts          エントリポイント
docs/samples/      サンプルごとの学習メモ
```

各サンプルは `Sample` 契約 (`src/core/types.ts`) に従い、`init` で `SampleHandle` を返します。レンダラ・キャンバス・アニメーションループはランタイムが一元管理し、サンプル側は資源を自分で `dispose` する責務を持ちます。

## 新しいサンプルを追加する

1. `src/samples/<your-id>/index.ts` を作成し、`Sample` を default export する。
2. `src/samples/index.ts` の `samples` 配列に追加する。
3. `docs/samples/<your-id>.md` に学習メモを書く。
4. `pnpm typecheck && pnpm check && pnpm build` を通す。
