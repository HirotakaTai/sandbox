// ============================================================================
// player.ts — プレイヤーキャラクター（青い球体）
// ----------------------------------------------------------------------------
// プレイヤーは XZ 平面上を「左右にだけ」動きます（前後はカメラ固定なので不要）。
//
// 学習ポイント:
//   - delta（前フレームからの経過秒数）を掛けることで、
//     FPS が変動しても移動速度が一定になる
//   - Math.max / Math.min で可動範囲をクランプ（はみ出し防止）
//   - 当たり判定用の「半径」を別途公開して、obstacles 側から参照
// ============================================================================

import { Mesh, MeshStandardMaterial, type Scene, SphereGeometry } from "three";
import type { InputState } from "./input";
import { WORLD_WIDTH } from "./scene";

/** プレイヤーの当たり判定半径（衝突計算用）。 */
export const PLAYER_RADIUS = 0.45;

/** プレイヤーの左右移動速度（m / 秒）。 */
const MOVE_SPEED = 7.5;

/** プレイヤーの Y 座標（球の中心が床の上に来る高さ）。 */
const PLAYER_Y = PLAYER_RADIUS;

export interface Player {
  /** シーンに追加されるメッシュ本体。当たり判定の位置参照にも使う。 */
  readonly mesh: Mesh;
  /** 入力に応じて 1 フレーム分プレイヤーを動かす。 */
  update(delta: number, input: InputState): void;
  /** 位置と角度を初期状態に戻す（リスタート時に使う）。 */
  reset(): void;
  /** GPU リソース解放。 */
  dispose(): void;
}

/**
 * プレイヤーを生成してシーンに追加します。
 *
 * @param scene  追加先のシーン（factory がシーン操作まで担当する設計）
 * @param playerZ プレイヤーを置く Z 座標
 */
export function createPlayer(scene: Scene, playerZ: number): Player {
  // 球を分割数 32 で作る。数が多いほど滑らかだが GPU 負荷も増える。
  const geometry = new SphereGeometry(PLAYER_RADIUS, 32, 24);
  const material = new MeshStandardMaterial({
    color: 0x4ea1ff,
    emissive: 0x1d3f7a, // ほのかに自己発光させて、暗い床でも目立たせる
    emissiveIntensity: 0.5,
    metalness: 0.2,
    roughness: 0.35,
  });

  const mesh = new Mesh(geometry, material);
  mesh.position.set(0, PLAYER_Y, playerZ);
  scene.add(mesh);

  // プレイヤーの可動範囲。走路の白線より少し内側にしておくと
  // 「壁にめり込まずに止まる」見た目になる。
  const halfRange = WORLD_WIDTH / 2 - PLAYER_RADIUS;

  function update(delta: number, input: InputState): void {
    let direction = 0;
    if (input.left) direction -= 1;
    if (input.right) direction += 1;

    // delta を掛けるのが超重要。これが無いと、
    // 「60fps と 144fps で移動速度が違う」という典型バグになる。
    mesh.position.x += direction * MOVE_SPEED * delta;

    // 走路からはみ出ないようにクランプ
    if (mesh.position.x < -halfRange) mesh.position.x = -halfRange;
    if (mesh.position.x > halfRange) mesh.position.x = halfRange;

    // 移動方向に少し傾けると「滑っている」感じが出る（演出）
    const targetTilt = direction * 0.35;
    // 線形補間で滑らかに目標角度へ近づける（イージングの一種）
    mesh.rotation.z += (targetTilt - mesh.rotation.z) * Math.min(1, delta * 8);

    // 走っているように見せるため、進行方向と垂直な X 軸まわりに回転
    mesh.rotation.x -= delta * 6;
  }

  function reset(): void {
    mesh.position.set(0, PLAYER_Y, playerZ);
    mesh.rotation.set(0, 0, 0);
  }

  function dispose(): void {
    geometry.dispose();
    material.dispose();
    scene.remove(mesh);
  }

  return { mesh, update, reset, dispose };
}
