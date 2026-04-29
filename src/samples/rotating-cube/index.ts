// ============================================================================
// rotating-cube — Three.js の最小構成サンプル
// ----------------------------------------------------------------------------
// このサンプルでは、Three.js でシーンを表示するために必要な
// 最小限のオブジェクトを揃えます。具体的には次の 3 つです:
//
//   1. Scene  : 描画する世界（オブジェクトを置く「箱」）
//   2. Camera : 世界をどこから・どんな画角で見るか
//   3. Mesh   : 形（Geometry）と見た目（Material）を組み合わせた物体
//
// レンダラ（WebGLRenderer）とアニメーションループ（requestAnimationFrame）は
// このサンプルでは持ちません。共有ランタイム（src/core/sample-runtime.ts）が
// 一括で管理しているためです。サンプル側は「何を描くか」だけに集中できます。
// ============================================================================

import { BoxGeometry, Color, Mesh, MeshNormalMaterial, PerspectiveCamera, Scene } from "three";
import type { Sample, SampleContext, SampleHandle } from "../../core/types";

/**
 * サンプルのメタデータ。
 * サイドバーや URL（`#/rotating-cube`）、ソースコードリンクなど、
 * UI のあちこちで使われます。
 */
const meta = {
  id: "rotating-cube",
  title: "回転するキューブ",
  summary: "Three.js の最小構成で立方体を回転させます。",
  description:
    "Scene / PerspectiveCamera / Mesh の最小構成です。法線マテリアル (MeshNormalMaterial) は光源不要で面の向きを色で表すため、ジオメトリの形状を直感的に確認できます。",
  tags: ["scene", "mesh", "animation"],
  difficulty: "beginner",
  sourcePath: "src/samples/rotating-cube/index.ts",
  docPath: "docs/samples/rotating-cube.md",
} as const;

/**
 * サンプルを初期化する関数。
 * ランタイムからキャンバスサイズなどの情報（context）が渡されます。
 * 戻り値の `SampleHandle` には、毎フレーム呼ばれる `update` や、
 * 後片付けの `dispose` を含めます。
 */
function init(context: SampleContext): SampleHandle {
  // --- Scene を作る ----------------------------------------------------------
  // Scene は「物を置く世界」です。背景色を指定すると、何も置いていない
  // 領域がその色で塗られます（指定しないと黒）。
  const scene = new Scene();
  scene.background = new Color(0x101218);

  // --- Camera を作る ---------------------------------------------------------
  // PerspectiveCamera は遠近感のあるカメラ（人間の目に近い見え方）です。
  // 引数は次のとおり:
  //   - fov   : 視野角 (degrees)。値が大きいほど広角。
  //   - aspect: 画面のアスペクト比（幅 / 高さ）。
  //   - near  : これより手前は描かれない。0 は不可。
  //   - far   : これより奥は描かれない。
  // ※ near と far の差を大きくしすぎると深度精度が落ちて
  //    Z-fighting（面のチラつき）が起きやすくなります。
  const camera = new PerspectiveCamera(45, context.width / context.height, 0.1, 100);

  // カメラを (2.5, 2, 3) に置き、原点を見るようにします。
  // Three.js は右手座標系で、+Y が上、+Z が画面手前です。
  camera.position.set(2.5, 2, 3);
  camera.lookAt(0, 0, 0);

  // --- Mesh（立方体）を作る --------------------------------------------------
  // BoxGeometry は立方体の頂点情報。
  // MeshNormalMaterial は「面の法線方向を RGB に変換して色付け」する
  // デバッグ向けマテリアルで、ライト無しでも面の向きが分かります。
  // 学習初期に「真っ黒で何も見えない」と詰まらないために便利です。
  const geometry = new BoxGeometry(1.2, 1.2, 1.2);
  const material = new MeshNormalMaterial();
  const cube = new Mesh(geometry, material);
  scene.add(cube);

  return {
    scene,
    camera,
    /**
     * 毎フレーム呼ばれる更新関数。
     * `delta` は「前フレームからの経過秒数」です。回転量に delta を
     * 掛けることで、フレームレートが変わっても回転速度が一定になります
     * （例えば 60fps と 144fps で同じ速さで回ります）。
     */
    update({ delta }) {
      cube.rotation.x += delta * 0.6; // 1 秒あたり約 0.6 ラジアン
      cube.rotation.y += delta * 0.9;
    },
    /**
     * 後片付け。サンプル切替時に必ず呼ばれます。
     *
     * Three.js の Geometry / Material / Texture は内部的に
     * GPU リソース（バッファやテクスチャ）を持っているため、
     * `.dispose()` を呼ばないとメモリリークします。
     * 「scene.remove() はあくまでシーングラフから外すだけで、
     * GPU リソースは解放しない」ことに注意してください。
     */
    dispose() {
      geometry.dispose();
      material.dispose();
      scene.remove(cube);
    },
  };
}

const sample: Sample = { meta, init };
export default sample;
