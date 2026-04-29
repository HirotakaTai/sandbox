# ライティング

3 種類のライトを組み合わせて、PBR マテリアル (`MeshStandardMaterial`) の見た目を比較するサンプルです。

## 学習目標

- `AmbientLight`（環境光）/ `DirectionalLight`（平行光源）/ `PointLight`（点光源）の役割を区別する。
- `metalness` / `roughness` がハイライトに与える影響を観察する。
- ライトの位置を時間で動かして、シーンが「生きている」感覚を得る。

> 注: このサンプルでは影（shadow map）は有効化していません。影を表現するには `renderer.shadowMap.enabled = true` と各ライト/メッシュの `castShadow`/`receiveShadow` を設定する必要があります。

## 関連 API

- [`THREE.AmbientLight`](https://threejs.org/docs/#api/en/lights/AmbientLight)
- [`THREE.DirectionalLight`](https://threejs.org/docs/#api/en/lights/DirectionalLight)
- [`THREE.PointLight`](https://threejs.org/docs/#api/en/lights/PointLight)
- [`THREE.MeshStandardMaterial`](https://threejs.org/docs/#api/en/materials/MeshStandardMaterial)

## ソース

`src/samples/lighting/index.ts`
