// ============================================================================
// postprocessing — EffectComposer + UnrealBloomPass で発光表現を試す
// ----------------------------------------------------------------------------
// ポストプロセス（後処理）は、レンダリング結果を 1 枚の画像として
// 受け取り、それに対してフィルタを掛けて最終出力する仕組みです。
//
// 構成は次のようになっています:
//   EffectComposer ─ RenderPass（普通にシーンを描く）
//                  └ UnrealBloomPass（明るい部分を抽出して光をにじませる）
//
// 特殊な事情として、サンプル側が独自に composer.render() を呼びたいので、
// このサンプルだけ `customRender` を使ってランタイムのデフォルト描画を
// 置き換えます。`update` はアニメーション専用、`customRender` は描画専用、
// と役割を分けて書いておくと後から読み返しやすくなります。
// ============================================================================

import {
  AmbientLight,
  Color,
  IcosahedronGeometry,
  Mesh,
  MeshStandardMaterial,
  PerspectiveCamera,
  PointLight,
  Scene,
  Vector2,
} from "three";
import { EffectComposer } from "three/examples/jsm/postprocessing/EffectComposer.js";
import { RenderPass } from "three/examples/jsm/postprocessing/RenderPass.js";
import { UnrealBloomPass } from "three/examples/jsm/postprocessing/UnrealBloomPass.js";
import type { Sample, SampleContext, SampleFrameInfo, SampleHandle } from "../../core/types";

const meta = {
  id: "postprocessing",
  title: "ポストプロセス（Bloom）",
  summary: "EffectComposer で UnrealBloomPass を適用し、発光表現を体験します。",
  description:
    "EffectComposer に RenderPass と UnrealBloomPass を積み重ねて、自己発光している球体を強調します。strength / radius / threshold の値を変えると、ブルームの広がり方が大きく変わります。",
  tags: ["postprocessing", "bloom", "composer"],
  difficulty: "advanced",
  sourcePath: "src/samples/postprocessing/index.ts",
  docPath: "docs/samples/postprocessing.md",
} as const;

function init(context: SampleContext): SampleHandle {
  const scene = new Scene();
  scene.background = new Color(0x05060a);

  const camera = new PerspectiveCamera(45, context.width / context.height, 0.1, 100);
  camera.position.set(0, 0.6, 4.5);

  // 環境光は弱めにし、自己発光（emissive）を主役にする
  const ambient = new AmbientLight(0xffffff, 0.15);
  const point = new PointLight(0xffffff, 1.4, 12, 1.5);
  point.position.set(2, 3, 2);
  scene.add(ambient, point);

  // --- 発光する物体 ---------------------------------------------------------
  // emissive を指定したマテリアルは「ライトに当たらなくても色を放つ」
  // ため、Bloom の対象として扱われやすくなります。
  // emissiveIntensity を上げると、ブルームの強さが増します。
  const geometry = new IcosahedronGeometry(0.9, 1);
  const material = new MeshStandardMaterial({
    color: 0xaa3bff,
    emissive: 0xaa3bff,
    emissiveIntensity: 1.6,
    metalness: 0.4,
    roughness: 0.25,
  });
  const mesh = new Mesh(geometry, material);
  scene.add(mesh);

  // --- EffectComposer の構築 -----------------------------------------------
  // EffectComposer は内部に複数の Pass を積み、上から順に処理します。
  // 1 段目に RenderPass（普通の描画）を置き、2 段目以降にエフェクトを足す
  // のが基本パターンです。
  const composer = new EffectComposer(context.renderer);

  // pixelRatio を渡し忘れると Retina など高 DPR 環境でぼやけて見えます。
  // renderer 側で setPixelRatio されているので同じ値を渡しておくのが安全。
  composer.setPixelRatio(context.renderer.getPixelRatio());
  composer.setSize(context.width, context.height);

  composer.addPass(new RenderPass(scene, camera));

  // UnrealBloomPass の主なパラメータ:
  //   - strength : 光のにじみの強さ
  //   - radius   : にじみの広がり半径
  //   - threshold: ここより明るいピクセルだけが Bloom の対象になる (0〜1)
  // threshold を 0 に近づけると、暗い部分まで光ってしまいがちです。
  const bloom = new UnrealBloomPass(
    new Vector2(context.width, context.height),
    1.2, // strength
    0.6, // radius
    0.05, // threshold
  );
  composer.addPass(bloom);

  /**
   * 毎フレームの「アニメーション更新」担当。
   * 描画はしません（描画は customRender 側に分離）。
   */
  function update(info: SampleFrameInfo): void {
    mesh.rotation.x += info.delta * 0.3;
    mesh.rotation.y += info.delta * 0.5;

    // 発光強度を時間で揺らして、Bloom の効きが変化する様子を見やすくする
    material.emissiveIntensity = 1.4 + Math.sin(info.elapsed * 1.6) * 0.6;
  }

  /**
   * このサンプルは composer 経由で描画したいので、
   * `customRender` を実装してランタイムのデフォルト描画を置き換えます。
   * （ランタイムは `customRender` があれば `renderer.render` を呼びません）
   */
  function customRender(info: SampleFrameInfo): void {
    composer.render(info.delta);
  }

  /**
   * リサイズ時の追従。
   * composer と bloom は内部レンダーターゲットを持っているので、
   * 両方にサイズと pixelRatio を再設定する必要があります。
   */
  function resize(width: number, height: number): void {
    composer.setPixelRatio(context.renderer.getPixelRatio());
    composer.setSize(width, height);
    bloom.setSize(width, height);
  }

  /**
   * 後片付け。Composer / Pass にも dispose があり、
   * 内部のレンダーターゲットを解放します。忘れるとリークします。
   */
  function dispose(): void {
    geometry.dispose();
    material.dispose();
    bloom.dispose();
    composer.dispose();
    scene.remove(mesh, ambient, point);
  }

  return { scene, camera, update, customRender, resize, dispose };
}

const sample: Sample = { meta, init };
export default sample;
