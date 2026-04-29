# OrbitControls でカメラ操作

マウスドラッグ / ホイール / タッチでカメラを回転・パン・ズームできるようにします。

## 学習目標

- `OrbitControls` の `target` / `minDistance` / `maxDistance` の意味を理解する。
- `enableDamping` を有効にして慣性付きの滑らかな操作にする。
- `controls.update()` を毎フレーム呼ぶ理由（damping を反映するため）を把握する。
- 破棄時に `controls.dispose()` を呼んでイベントリスナーを掃除する。

## 関連 API

- [`THREE.OrbitControls`](https://threejs.org/docs/#examples/en/controls/OrbitControls)

## ソース

`src/samples/controls/index.ts`
