// ============================================================================
// controls — OrbitControls でカメラをマウス操作する
// ----------------------------------------------------------------------------
// OrbitControls は Three.js 標準アドオンの 1 つで、
//   - 左ドラッグ : 注視点を中心に回転
//   - 右ドラッグ : パン（平行移動）
//   - ホイール   : ズームイン / アウト
// を実現してくれます。
//
// 初学者がハマりやすい点:
//   - `controls.update()` を毎フレーム呼ばないと、enableDamping が機能しない
//   - dispose 時に `controls.dispose()` を呼ばないと
//     キャンバスにイベントリスナーが残り続ける（メモリリーク）
//   - new OrbitControls の第 2 引数は「DOM 要素」。renderer.domElement
//     ＝ ランタイムから渡される `context.canvas` を渡します。
// ============================================================================

import {
  AmbientLight,
  BoxGeometry,
  Color,
  DirectionalLight,
  Group,
  Mesh,
  MeshStandardMaterial,
  PerspectiveCamera,
  Scene,
} from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import type { Sample, SampleContext, SampleHandle } from "../../core/types";

const meta = {
  id: "controls",
  title: "OrbitControls でカメラ操作",
  summary: "マウスドラッグやホイールでカメラを操作できるようにします。",
  description:
    "OrbitControls はカメラを注視点中心に回転・パン・ズームさせるユーティリティです。enableDamping を有効にすると慣性のある滑らかな操作になります。マウス/タッチで自由に視点を変えてみてください。",
  tags: ["controls", "camera", "interaction"],
  difficulty: "intermediate",
  sourcePath: "src/samples/controls/index.ts",
  docPath: "docs/samples/controls.md",
} as const;

function init(context: SampleContext): SampleHandle {
  const scene = new Scene();
  scene.background = new Color(0x0e1117);

  const camera = new PerspectiveCamera(45, context.width / context.height, 0.1, 100);
  camera.position.set(4, 3, 5);

  // --- OrbitControls を構成 -------------------------------------------------
  // 第 2 引数の DOM 要素が「マウス/タッチを受け付ける範囲」になります。
  // ここでは共有レンダラのキャンバス（context.canvas）を渡します。
  const controls = new OrbitControls(camera, context.canvas);

  // ドラッグを離したあとも少しだけ回転が続く「慣性」を有効化。
  // 体感が滑らかになり、操作の気持ち良さが大きく変わります。
  controls.enableDamping = true;
  controls.dampingFactor = 0.08;

  // ズーム範囲の制限。これを設定しないと、被写体に潜り込んだり
  // 遠くに飛ばされて行方不明になりがちです。
  controls.minDistance = 2;
  controls.maxDistance = 12;

  // 注視点。OrbitControls はこの点を中心に回転します。
  // ここを動かすとカメラが「どこを見ているか」を変えられます。
  controls.target.set(0, 0.5, 0);
  controls.update();

  // --- 観察対象として小さなキューブの 3x3 グリッドを置く -------------------
  const group = new Group();
  const geometry = new BoxGeometry(0.6, 0.6, 0.6);

  // すべての Mesh で同じ Geometry を使い回し、
  // 色だけ Material で変えます。Geometry の使い回しは
  // GPU メモリの節約に有効です（Three.js でよく使う最適化）。
  const materials: MeshStandardMaterial[] = [];
  const palette = [0xaa3bff, 0x4ea1ff, 0xff7a59, 0x59ffa6, 0xffd05a];
  let i = 0;
  for (let x = -1; x <= 1; x += 1) {
    for (let z = -1; z <= 1; z += 1) {
      const material = new MeshStandardMaterial({
        color: palette[i % palette.length],
        roughness: 0.4,
        metalness: 0.1,
      });
      const mesh = new Mesh(geometry, material);
      mesh.position.set(x * 1.1, 0.3 + Math.abs(x + z) * 0.15, z * 1.1);
      group.add(mesh);
      materials.push(material);
      i += 1;
    }
  }
  scene.add(group);

  const ambient = new AmbientLight(0xffffff, 0.45);
  const directional = new DirectionalLight(0xffffff, 1.0);
  directional.position.set(5, 6, 3);
  scene.add(ambient, directional);

  return {
    scene,
    camera,
    update({ delta }) {
      // 観察しやすいように、グループ全体もゆっくり回しておく。
      group.rotation.y += delta * 0.15;

      // damping を有効にしている場合、毎フレームの update() 呼び出しが必須。
      // 呼び忘れると「ドラッグを離した瞬間にピタッと止まる」挙動になります。
      controls.update();
    },
    dispose() {
      // OrbitControls は内部で複数のイベントリスナー（pointerdown, wheel など）を
      // canvas に登録しています。dispose() で必ず外しましょう。
      controls.dispose();
      geometry.dispose();
      for (const material of materials) material.dispose();
      // group.remove(...) は不要。scene.remove(group) でツリーごと外せばよい。
      scene.remove(group, ambient, directional);
    },
  };
}

const sample: Sample = { meta, init };
export default sample;
