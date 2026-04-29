// ============================================================================
// obstacles.ts — 障害物（赤いキューブ）の生成 / 移動 / 当たり判定
// ----------------------------------------------------------------------------
// 障害物は奥（負の Z）からプレイヤーに向かって流れてきます。
// プレイヤー側を通り過ぎたら「再利用」して、また奥にワープさせる
// ＝ オブジェクトプールパターンを使っています。
//
// なぜプールするのか:
//   - 毎回 new Mesh() / dispose() を繰り返すと GC やヒープ確保のコストが
//     積み重なり、ゲームがガクつく原因になります。
//   - 同じ Geometry / Material を共有しつつ、Mesh のインスタンスだけ
//     ぐるぐる使い回すのが王道のテクニックです。
// ============================================================================

import { BoxGeometry, Mesh, MeshStandardMaterial, type Scene } from "three";
import { PLAYER_RADIUS } from "./player";
import { DESPAWN_Z, SPAWN_Z, WORLD_WIDTH } from "./scene";

/** 1 つの障害物の半径（立方体の半サイズ）。当たり判定にも使う。 */
const OBSTACLE_HALF = 0.5;

/** 同時に存在する障害物の最大数（プールサイズ）。 */
const POOL_SIZE = 14;

/** 新しい障害物がスポーンする時間間隔の基準（秒）。 */
const BASE_SPAWN_INTERVAL = 0.9;

/** 障害物が手前へ流れてくる基準速度（m / 秒）。 */
const BASE_FORWARD_SPEED = 9;

/**
 * プール内の 1 体ぶんの状態。
 * `alive: false` のときはシーンに表示されておらず、再利用待ち。
 */
interface ObstacleSlot {
  mesh: Mesh;
  alive: boolean;
}

export interface ObstacleField {
  /**
   * 1 フレーム分、障害物を進めて生成と消去を行う。
   * @param speedMultiplier ゲーム進行に応じて増加する速度倍率（1.0 始まり）
   */
  update(delta: number, speedMultiplier: number): void;
  /**
   * プレイヤー位置との衝突を判定。
   * AABB（軸並行境界ボックス）の重なりで近似します。
   * @param playerX  プレイヤーの X 座標
   * @param playerZ  プレイヤーの Z 座標
   * @returns ヒットしたら true
   */
  checkCollision(playerX: number, playerZ: number): boolean;
  /** すべての障害物を非表示にして、初期状態に戻す（リスタート時）。 */
  reset(): void;
  dispose(): void;
}

/**
 * 障害物フィールドを生成してシーンに追加します。
 */
export function createObstacleField(scene: Scene): ObstacleField {
  // Geometry / Material はプール全体で 1 つを共有。
  // メモリ効率と GPU の draw call 削減に有効。
  const geometry = new BoxGeometry(OBSTACLE_HALF * 2, OBSTACLE_HALF * 2, OBSTACLE_HALF * 2);
  const material = new MeshStandardMaterial({
    color: 0xff5a5a,
    emissive: 0x6a1818,
    emissiveIntensity: 0.7,
    roughness: 0.45,
    metalness: 0.1,
  });

  // プールを構築。最初は全て alive=false（非表示）。
  const slots: ObstacleSlot[] = [];
  for (let i = 0; i < POOL_SIZE; i += 1) {
    const mesh = new Mesh(geometry, material);
    mesh.visible = false; // プール待機中は描画しない
    scene.add(mesh);
    slots.push({ mesh, alive: false });
  }

  /** 経過時間を蓄積して「次の生成タイミング」を判定するためのカウンタ。 */
  let spawnTimer = 0;

  /**
   * 待機中（alive=false）のスロットを 1 つ探して、奥から流し直す。
   * 全部使用中なら何もしない。
   */
  function spawn(): void {
    const slot = slots.find((s) => !s.alive);
    if (!slot) return;
    slot.alive = true;
    slot.mesh.visible = true;
    // X 位置はランダムだが、走路をはみ出さない範囲。
    const halfRange = WORLD_WIDTH / 2 - OBSTACLE_HALF;
    slot.mesh.position.set((Math.random() * 2 - 1) * halfRange, OBSTACLE_HALF, SPAWN_Z);
    // 見た目に変化をつけるため、角度もランダムに
    slot.mesh.rotation.set(
      Math.random() * Math.PI,
      Math.random() * Math.PI,
      Math.random() * Math.PI,
    );
  }

  function update(delta: number, speedMultiplier: number): void {
    const speed = BASE_FORWARD_SPEED * speedMultiplier;

    // 既存の障害物を手前へ進める
    for (const slot of slots) {
      if (!slot.alive) continue;
      slot.mesh.position.z += speed * delta;
      // 回転させると見た目に動きが出て、迫力アップ
      slot.mesh.rotation.x += delta * 1.4;
      slot.mesh.rotation.y += delta * 0.9;

      // プレイヤーを通り過ぎたらプールに戻す
      if (slot.mesh.position.z > DESPAWN_Z) {
        slot.alive = false;
        slot.mesh.visible = false;
      }
    }

    // 生成タイミング: 速度が上がるほど間隔を短くしていく
    spawnTimer += delta;
    const interval = BASE_SPAWN_INTERVAL / speedMultiplier;
    if (spawnTimer >= interval) {
      spawnTimer = 0;
      spawn();
    }
  }

  function checkCollision(playerX: number, playerZ: number): boolean {
    // AABB の判定。Z 方向は障害物の半径 + プレイヤーの半径だけ余裕を見て、
    // X 方向も同様。本来は球と直方体の正確な判定もできるが、
    // 学習サンプルでは十分実用的な近似です。
    const dz = OBSTACLE_HALF + PLAYER_RADIUS;
    const dx = OBSTACLE_HALF + PLAYER_RADIUS;
    for (const slot of slots) {
      if (!slot.alive) continue;
      const p = slot.mesh.position;
      if (Math.abs(p.x - playerX) < dx && Math.abs(p.z - playerZ) < dz) {
        return true;
      }
    }
    return false;
  }

  function reset(): void {
    for (const slot of slots) {
      slot.alive = false;
      slot.mesh.visible = false;
    }
    spawnTimer = 0;
  }

  function dispose(): void {
    // 共有 Geometry / Material は最後に 1 度だけ dispose。
    geometry.dispose();
    material.dispose();
    for (const slot of slots) {
      scene.remove(slot.mesh);
    }
  }

  return { update, checkCollision, reset, dispose };
}
