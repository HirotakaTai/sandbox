# ポストプロセス（Bloom）

`EffectComposer` を使って、自己発光している球体に Bloom（光のにじみ）を加えます。

## 学習目標

- `EffectComposer` に `RenderPass` と効果用 Pass を積み重ねる構造を理解する。
- `UnrealBloomPass` の `strength` / `radius` / `threshold` を変えて挙動を比較する。
- サンプル側でレンダリングを行うとき、`customRender` を実装するとランタイムのデフォルト描画が抑止される。
- リサイズ時に `composer.setSize()` と `bloom.setSize()` を呼ぶ。

## 関連 API

- [`EffectComposer`](https://threejs.org/docs/#examples/en/postprocessing/EffectComposer)
- [`RenderPass`](https://threejs.org/docs/#examples/en/postprocessing/RenderPass)
- [`UnrealBloomPass`](https://threejs.org/docs/#examples/en/postprocessing/UnrealBloomPass)

## ソース

`src/samples/postprocessing/index.ts`
