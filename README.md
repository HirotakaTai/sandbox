# Three.js 学習サンプル

Vite + TypeScript + Three.js + Biome で構築した、Three.js の基本機能を 1 つずつ確認できる学習者向けサンプルブラウザです。

機能単体を学ぶ `src/samples/` に加えて、複数概念を組み合わせる `src/games/`（ミニゲーム）も含みます。

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
| `dodge-runner` | 障害物回避ミニゲーム（入力・当たり判定・状態管理） | 上級 |

各サンプルの詳細は [`docs/samples/`](./docs/samples/) を参照してください。

## アーキテクチャ概要

```
src/
  app/             サイドバー / ヘッダ / ハッシュルータ（UI シェル）
  core/            サンプルランタイム（共有 WebGLRenderer / アニメーションループ）
  samples/<id>/    単機能サンプル（init / update / dispose ライフサイクル）
  games/<id>/      複数概念を組み合わせた学習用ミニゲーム
  styles/          アプリ全体の CSS
  main.ts          エントリポイント
docs/samples/      サンプルごとの学習メモ
```

`src/samples/` と `src/games/` のどちらも `Sample` 契約 (`src/core/types.ts`) に従い、`init` で `SampleHandle` を返します。レンダラ・キャンバス・アニメーションループはランタイムが一元管理し、サンプル側は資源を自分で `dispose` する責務を持ちます。

## 新しい学習コンテンツを追加する

1. 単機能なら `src/samples/<your-id>/index.ts`、ミニゲームなら `src/games/<your-id>/index.ts` を作成し、`Sample` を default export する。
2. `src/samples/index.ts` の `samples` 配列に追加する。
3. `docs/samples/<your-id>.md` に学習メモを書く。
4. `pnpm typecheck && pnpm check && pnpm build` を通す。
