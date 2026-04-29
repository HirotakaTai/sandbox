// ============================================================================
// dodge-runner / index.ts — ゲーム本体（Sample 契約のエントリ）
// ----------------------------------------------------------------------------
// このファイルは「Sample インターフェースを満たすラッパー」です。
// 実際のロジックは scene / player / obstacles / input / hud / game-state に
// 分割されています。ここでは **配線（wiring）** だけを行います。
//
// ゲームジャンル: 簡単な “ドッジランナー”
//   - 青い球体（プレイヤー）を左右に動かして、
//     奥から流れてくる赤いキューブを避け続ける
//   - 生き延びた秒数がスコア
//   - ぶつかったら GameOver、R キーでリスタート
//
// 学習ポイント:
//   - 1 つの Sample を「複数モジュールの組み合わせ」として作る方法
//   - delta-time に基づくフレームレート非依存のロジック
//   - オブジェクトプール（obstacles.ts 参照）
//   - 状態機械でゲームのフェーズを安全に管理する（game-state.ts 参照）
// ============================================================================

import type { Sample, SampleContext, SampleHandle } from "../../core/types";
import { createGameState } from "./game-state";
import { createHud } from "./hud";
import { createInput } from "./input";
import { createObstacleField } from "./obstacles";
import { createPlayer } from "./player";
import { createGameScene, PLAYER_Z } from "./scene";

const meta = {
  id: "dodge-runner",
  title: "Dodge Runner（ゲーム）",
  summary: "奥から飛んでくる障害物を避け続ける、はじめての Three.js ゲーム。",
  description:
    "← / → または A / D で青い球体を左右に動かし、赤いキューブを避け続けるゲームです。生存時間がスコアになり、進むほど速度が上がっていきます。複数モジュールに責務を分けたゲーム実装の最小例として、`src/games/dodge-runner/` 以下を読んでみてください。",
  tags: ["game", "input", "collision"],
  difficulty: "advanced",
  sourcePath: "src/games/dodge-runner/index.ts",
  docPath: "docs/samples/dodge-runner.md",
} as const;

function init(context: SampleContext): SampleHandle {
  // --- 各モジュールを構築 ---------------------------------------------------
  // ここで作る順序は依存関係に従う:
  //   scene → (player / obstacles はシーンを必要とする)
  //   input / state / hud は独立
  const sceneBundle = createGameScene(context.width / context.height);
  const player = createPlayer(sceneBundle.scene, PLAYER_Z);
  const obstacles = createObstacleField(sceneBundle.scene);
  const input = createInput();
  const state = createGameState();
  // HUD は canvas 上に DOM を被せたいので、canvas の親要素を渡す。
  // 親要素が必ず存在する保証はないので、念のためフォールバックも用意。
  const hudHost = context.canvas.parentElement ?? document.body;
  const hud = createHud(hudHost);

  function update({ delta }: { delta: number }): void {
    // ── R キーが押されたらリスタート（playing 中でもいつでも受け付ける） ──
    if (input.consumeRestart()) {
      state.restart();
      player.reset();
      obstacles.reset();
    }

    // ── 状態に応じた更新 ─────────────────────────────────────────────────
    if (state.phase === "playing") {
      // playing 中だけプレイヤーと障害物を動かす。
      // GameOver 中も動かしてしまうと「死んだのに障害物がスコアの裏で進む」
      // ような不自然さが出るので、状態でガードするのが鉄則。
      player.update(delta, input.state);
      obstacles.update(delta, state.speedMultiplier);
      state.tick(delta);

      // 当たり判定 → 衝突したら状態遷移
      if (obstacles.checkCollision(player.mesh.position.x, player.mesh.position.z)) {
        state.gameOver();
      }
    }

    // HUD は毎フレーム更新（スコア表示が滑らかになるように）
    hud.update(state);
  }

  return {
    scene: sceneBundle.scene,
    camera: sceneBundle.camera,
    update,
    dispose() {
      // 作った順と逆順で片付けるのが安全（依存先を後に消す）。
      hud.dispose();
      input.dispose();
      obstacles.dispose();
      player.dispose();
      sceneBundle.dispose();
    },
  };
}

const sample: Sample = { meta, init };
export default sample;
