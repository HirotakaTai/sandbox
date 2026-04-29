# src/games — Three.js を使った学習用ミニゲーム

`src/samples/` が「Three.js の機能 1 つに焦点を絞った最小サンプル集」なのに対して、
こちらは **複数の Three.js の概念を組み合わせた “動くもの” を体験する** ためのディレクトリです。

## 設計方針

ゲームになると 1 ファイル 200 行・300 行と肥大化しがちで、初学者が
「どこで何が起きているのか」を読み解きづらくなります。
そこで本ディレクトリのゲームは **責務ごとにファイルを分割** しています。

例: `dodge-runner/` の構成

```
dodge-runner/
  index.ts        ← Sample 契約のエントリ。各モジュールを配線するだけ
  scene.ts        ← シーン / カメラ / ライト / 床（見た目の土台）
  player.ts       ← プレイヤーオブジェクト（メッシュ + 入力反映）
  obstacles.ts    ← 障害物のスポーン / 移動 / 当たり判定
  input.ts        ← キーボード入力の状態化（左右 / リスタート）
  hud.ts          ← スコア表示や Game Over オーバーレイ（DOM）
  game-state.ts   ← ゲームの状態機械とスコア管理
```

各ファイルは「**factory 関数 + 戻り値オブジェクト**」のシンプルなパターンで統一しています。

```ts
export function createPlayer(scene: Scene): Player {
  // 構築...
  return {
    mesh,
    update(delta, input) { ... },
    dispose() { ... },
  };
}
```

クラスや継承は使わず、関数とオブジェクトだけで構成しているので、
「new と this の挙動」に悩まずに、ロジックそのものに集中できます。

## ゲームを追加する手順

1. `src/games/<game-id>/` ディレクトリを作る
2. 上記パターンに沿って必要なモジュールを実装
3. `index.ts` で `Sample` を default export
4. `src/samples/index.ts` の `samples` 配列に追加

これだけでサイドバーに表示され、URL ルーティング (`#/<id>`) でも開けるようになります。
