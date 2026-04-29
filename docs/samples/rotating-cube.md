# 回転するキューブ

最小構成で Three.js のレンダリングフローを把握するためのサンプルです。

## 学習目標

- `Scene` / `PerspectiveCamera` / `Mesh` の関係を理解する。
- `BoxGeometry` と `MeshNormalMaterial` で「ライト無しでも形状が見える」状態を作る。
- ランタイムから渡される `delta`（前フレームからの経過秒）で時間に依存しない回転を実装する。

## 関連 API

- [`THREE.Scene`](https://threejs.org/docs/#api/en/scenes/Scene)
- [`THREE.PerspectiveCamera`](https://threejs.org/docs/#api/en/cameras/PerspectiveCamera)
- [`THREE.BoxGeometry`](https://threejs.org/docs/#api/en/geometries/BoxGeometry)
- [`THREE.MeshNormalMaterial`](https://threejs.org/docs/#api/en/materials/MeshNormalMaterial)

## ソース

`src/samples/rotating-cube/index.ts`
