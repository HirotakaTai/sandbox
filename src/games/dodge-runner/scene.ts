// ============================================================================
// scene.ts — ゲームシーンの「見た目の土台」を構築する
// ----------------------------------------------------------------------------
// Scene / Camera / ライト / 床 / 走路の白線 を 1 か所にまとめます。
// 「ゲームのロジック」とは関係ない部分なので分離しておくと、
// あとからライトの色を変えたいだけ、といった小さな変更がしやすくなります。
// ============================================================================

import {
  AmbientLight,
  Color,
  DirectionalLight,
  Fog,
  Mesh,
  MeshStandardMaterial,
  PerspectiveCamera,
  PlaneGeometry,
  Scene,
} from "three";

/**
 * ゲームの世界の幅（X 方向）。
 * プレイヤーの可動範囲、障害物のスポーン X 範囲、床の幅、すべての基準。
 */
export const WORLD_WIDTH = 6;

/** プレイヤーが存在する Z 座標（カメラ寄り）。 */
export const PLAYER_Z = 4;

/** 障害物がスポーンする Z 座標（奥）。 */
export const SPAWN_Z = -40;

/** 障害物がここを過ぎたら消去 / 再利用する Z 座標。 */
export const DESPAWN_Z = 8;

/**
 * シーン構築関数の戻り値。
 * `dispose()` でシーン内に作った GPU リソースを全て解放します。
 */
export interface GameScene {
  readonly scene: Scene;
  readonly camera: PerspectiveCamera;
  dispose(): void;
}

/**
 * ゲームシーンを構築して返します。
 *
 * @param aspect 初期アスペクト比（width / height）
 */
export function createGameScene(aspect: number): GameScene {
  const scene = new Scene();
  scene.background = new Color(0x0a0c14);

  // Fog を効かせると「奥から障害物がだんだん見えてくる」演出になり、
  // ゲームらしい奥行き感が出ます。色は背景と揃えるのがコツ。
  scene.fog = new Fog(0x0a0c14, 12, 40);

  // 透視投影カメラ。fov を少し広めにすると「飛び出してくる」迫力が増します。
  const camera = new PerspectiveCamera(60, aspect, 0.1, 100);
  camera.position.set(0, 3.2, 8.5);
  camera.lookAt(0, 0.5, 0);

  // --- ライト ---------------------------------------------------------------
  // 環境光: 全体の最低明度を確保（影が真っ黒にならないように）
  const ambient = new AmbientLight(0xffffff, 0.45);
  // 平行光源: ハイライトを作って立体感を出す。位置は方向ベクトルとして扱われる。
  const directional = new DirectionalLight(0xffffff, 1.1);
  directional.position.set(4, 8, 6);
  scene.add(ambient, directional);

  // --- 床 -------------------------------------------------------------------
  // 床は十分に長くしておく（カメラから奥まで地面が続いて見えるように）。
  // PlaneGeometry はデフォルトで XY 平面なので、X 軸まわりに -90° 回転して
  // XZ 平面（地面）にする。これは Three.js でとても頻繁に使うイディオム。
  const floorGeo = new PlaneGeometry(WORLD_WIDTH * 1.6, 80);
  const floorMat = new MeshStandardMaterial({
    color: 0x141821,
    roughness: 0.95,
    metalness: 0.0,
  });
  const floor = new Mesh(floorGeo, floorMat);
  floor.rotation.x = -Math.PI / 2;
  floor.position.z = -20; // 奥側にずらして、走路として見えるようにする
  scene.add(floor);

  // --- 走路の左右の白線（壁のかわり）---------------------------------------
  // プレイヤーの可動範囲の境界を視覚的に示すための装飾。
  // 細長い PlaneGeometry を 2 枚並べるだけ。
  const lineGeo = new PlaneGeometry(0.08, 80);
  const lineMat = new MeshStandardMaterial({
    color: 0xaa3bff,
    emissive: 0x6a1dbf, // ほんのり光らせて視認性を上げる
    emissiveIntensity: 0.6,
    roughness: 0.4,
  });
  const leftLine = new Mesh(lineGeo, lineMat);
  leftLine.rotation.x = -Math.PI / 2;
  leftLine.position.set(-WORLD_WIDTH / 2, 0.01, -20);
  const rightLine = new Mesh(lineGeo, lineMat);
  rightLine.rotation.x = -Math.PI / 2;
  rightLine.position.set(WORLD_WIDTH / 2, 0.01, -20);
  scene.add(leftLine, rightLine);

  return {
    scene,
    camera,
    dispose() {
      // 作った GPU リソースを忘れずに解放する（リーク防止）。
      // Geometry は `lineGeo` を 2 枚で共有しているので 1 回だけ dispose で OK。
      floorGeo.dispose();
      floorMat.dispose();
      lineGeo.dispose();
      lineMat.dispose();
      scene.remove(floor, leftLine, rightLine, ambient, directional);
    },
  };
}
