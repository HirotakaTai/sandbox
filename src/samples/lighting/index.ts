// ============================================================================
// lighting — ライトとマテリアルの関係を学ぶサンプル
// ----------------------------------------------------------------------------
// MeshNormalMaterial と違い、MeshStandardMaterial はライトが無いと真っ黒に
// なります。ここでは 3 種類のライトを置き、それぞれの役割を視覚的に
// 確認できるようにします。
//
//   - AmbientLight     : シーン全体を等しく照らす「環境光」。影は作らない。
//   - DirectionalLight : 太陽のように、平行な方向からまっすぐ照らす光。
//   - PointLight       : 電球のように、一点から放射状に広がる光。
//
// このサンプルでは「シャドウマップ（落ち影）」は意図的に有効化していません。
// 影を出すには renderer.shadowMap.enabled や mesh.castShadow などの設定が
// 必要で、初学者には情報量が多すぎるためです。
// ============================================================================

import {
  AmbientLight,
  Color,
  DirectionalLight,
  Mesh,
  MeshStandardMaterial,
  PerspectiveCamera,
  PlaneGeometry,
  PointLight,
  Scene,
  SphereGeometry,
  TorusKnotGeometry,
} from "three";
import type { Sample, SampleContext, SampleHandle } from "../../core/types";

const meta = {
  id: "lighting",
  title: "ライティング",
  summary: "AmbientLight / DirectionalLight / PointLight の効果を比較します。",
  description:
    "MeshStandardMaterial と 3 種類のライトを組み合わせ、PBR の質感とハイライトの動きを観察します。PointLight をゆっくり周回させて、表面のハイライトがどう変化するかを確認できます。",
  tags: ["lighting", "material", "PBR"],
  difficulty: "beginner",
  sourcePath: "src/samples/lighting/index.ts",
  docPath: "docs/samples/lighting.md",
} as const;

function init(context: SampleContext): SampleHandle {
  const scene = new Scene();
  scene.background = new Color(0x0b0d12);

  const camera = new PerspectiveCamera(45, context.width / context.height, 0.1, 100);
  camera.position.set(3, 2.5, 5);
  camera.lookAt(0, 0.4, 0);

  // --- Lights ---------------------------------------------------------------
  // 環境光は「最低限の明るさ」を底上げする役目。これが無いと、
  // 直接光が当たっていない面が完全に黒くなり、立体感が失われます。
  const ambient = new AmbientLight(0xffffff, 0.25);

  // 平行光源は太陽光のイメージ。`position` は「光がどこから来るか」の
  // 方向を示すベクトルとして解釈されます（位置そのものではなく方向）。
  const directional = new DirectionalLight(0xffffff, 1.1);
  directional.position.set(4, 6, 3);

  // 点光源は位置 + 距離減衰 + 減衰係数の組み合わせ。
  // `distance` を 0 にすると無限遠まで届き、減衰しなくなります。
  const point = new PointLight(0xff7a59, 1.5, 8, 1.5);
  point.position.set(0, 1.5, 0);

  scene.add(ambient, directional, point);

  // --- Geometry & Material -------------------------------------------------
  // MeshStandardMaterial は PBR（物理ベースレンダリング）に基づくマテリアル。
  //   - metalness: 金属らしさ。0 = 非金属、1 = 金属。
  //   - roughness: 表面の粗さ。0 = 鏡面、1 = マット。
  // 値を変えるとハイライトの広がり方が大きく変わるので、ぜひ書き換えて
  // 試してみてください。
  const knotGeo = new TorusKnotGeometry(0.6, 0.18, 180, 24);
  const knotMat = new MeshStandardMaterial({
    color: 0xc9c9d1,
    metalness: 0.4,
    roughness: 0.35,
  });
  const knot = new Mesh(knotGeo, knotMat);
  knot.position.y = 0.6;

  const sphereGeo = new SphereGeometry(0.35, 32, 32);
  const sphereMat = new MeshStandardMaterial({ color: 0x4ea1ff, roughness: 0.2, metalness: 0.1 });
  const sphere = new Mesh(sphereGeo, sphereMat);
  sphere.position.set(1.4, 0.35, 0.6);

  // 床。PlaneGeometry はデフォルトで XY 平面を向いているので、
  // X 軸まわりに -90° 回転させて XZ 平面（地面）にします。
  const floorGeo = new PlaneGeometry(8, 8);
  const floorMat = new MeshStandardMaterial({ color: 0x1c1f26, roughness: 0.9 });
  const floor = new Mesh(floorGeo, floorMat);
  floor.rotation.x = -Math.PI / 2;

  scene.add(knot, sphere, floor);

  return {
    scene,
    camera,
    update({ delta, elapsed }) {
      // トーラスノットをゆっくり回転（質感の見え方の変化を観察するため）
      knot.rotation.y += delta * 0.4;
      knot.rotation.x += delta * 0.2;

      // 点光源を XZ 平面で円運動させる。`elapsed` は「サンプル開始からの
      // 経過秒数」なので、sin/cos に渡すだけで滑らかな周回になります。
      point.position.x = Math.cos(elapsed * 0.8) * 1.6;
      point.position.z = Math.sin(elapsed * 0.8) * 1.6;
    },
    dispose() {
      // Geometry / Material は明示的に dispose する必要があります。
      // ライトオブジェクト自体は GPU リソースを持たないので
      // scene から remove するだけで十分です。
      knotGeo.dispose();
      knotMat.dispose();
      sphereGeo.dispose();
      sphereMat.dispose();
      floorGeo.dispose();
      floorMat.dispose();
      scene.remove(knot, sphere, floor, ambient, directional, point);
    },
  };
}

const sample: Sample = { meta, init };
export default sample;
