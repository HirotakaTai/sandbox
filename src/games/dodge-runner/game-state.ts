// ============================================================================
// game-state.ts — ゲームの状態機械（state machine）
// ----------------------------------------------------------------------------
// 「いまゲームはどの状態か？」を 1 か所で管理するためのモジュールです。
// 状態が散らばっていると、リスタート漏れ・スコア二重加算・GameOver 中も
// プレイヤーが動く、といった分かりにくいバグが入りがちです。
//
// 状態遷移は以下の 1 本道:
//
//     ┌──────────┐  collision   ┌──────────┐
//     │ playing  │ ───────────▶ │ gameover │
//     └──────────┘              └──────────┘
//          ▲                          │
//          └────── restart() ─────────┘
// ============================================================================

/** ゲームのフェーズ。 */
export type Phase = "playing" | "gameover";

/** ゲーム状態の現在値（読み取り専用 view）。 */
export interface GameStateView {
  readonly phase: Phase;
  /** プレイ開始からの生存時間（秒）。スコアとして表示。 */
  readonly score: number;
  /** これまでの最高スコア（このセッション中だけ覚えておく）。 */
  readonly best: number;
  /** 進行に応じて 1.0 → ゆるやかに増えていく速度倍率。 */
  readonly speedMultiplier: number;
}

export interface GameState extends GameStateView {
  /** 1 フレーム進める（playing 中のみスコア / 速度を更新）。 */
  tick(delta: number): void;
  /** ゲームオーバーへ遷移。すでに gameover なら何もしない。 */
  gameOver(): void;
  /** 初期状態に戻す（再プレイ）。 */
  restart(): void;
}

/**
 * 速度倍率の伸び方。
 * - 線形だと終盤すぐ理不尽になり
 * - 指数的だと序盤が退屈になりがち
 * のため、平方根ベースで「最初は早く伸び、徐々に緩やかになる」カーブにする。
 */
function computeSpeedMultiplier(score: number): number {
  return 1 + Math.sqrt(score) * 0.18;
}

export function createGameState(): GameState {
  let phase: Phase = "playing";
  let score = 0;
  let best = 0;
  let speedMultiplier = 1;

  function tick(delta: number): void {
    if (phase !== "playing") return;
    score += delta;
    speedMultiplier = computeSpeedMultiplier(score);
  }

  function gameOver(): void {
    if (phase === "gameover") return; // 二重遷移防止（スコア固定のため）
    phase = "gameover";
    if (score > best) best = score;
  }

  function restart(): void {
    phase = "playing";
    score = 0;
    speedMultiplier = 1;
  }

  // getter で公開することで、外部からの直接書き換えを防ぎつつ
  // 常に最新の値を読めるようにする。
  return {
    get phase() {
      return phase;
    },
    get score() {
      return score;
    },
    get best() {
      return best;
    },
    get speedMultiplier() {
      return speedMultiplier;
    },
    tick,
    gameOver,
    restart,
  };
}
