# テクスチャマッピング

外部ファイルを使わず、`<canvas>` から `CanvasTexture` を作成して立方体に貼り付けます。

## 学習目標

- `CanvasTexture` でプロシージャルなテクスチャを作る方法を知る。
- `wrapS` / `wrapT` と `repeat` で UV の繰り返し方を制御する。
- `colorSpace = SRGBColorSpace` を設定して色を正しく表示する。

## 試してみる

- `repeat.set(2, 2)` を `(1, 1)` や `(4, 4)` に変えてみる。
- `wrapS` / `wrapT` を `ClampToEdgeWrapping` に変えると端の挙動が変わります。

## 関連 API

- [`THREE.CanvasTexture`](https://threejs.org/docs/#api/en/textures/CanvasTexture)
- [`THREE.RepeatWrapping`](https://threejs.org/docs/#api/en/constants/Textures)

## ソース

`src/samples/texture/index.ts`
