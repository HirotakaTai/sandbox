// ============================================================================
// hud.ts — DOM オーバーレイによる HUD（スコア表示・Game Over 画面）
// ----------------------------------------------------------------------------
// 3D 内に文字を描く方法は複数ありますが（Sprite, CSS2DRenderer, TextGeometry…）、
// 学習用の HUD なら **canvas の上に DOM をかぶせる** のが一番簡単で軽量です。
//
// やっていること:
//   - canvas の親要素 (canvas-host) に <div> を absolute で重ねる
//   - スコアや状態に応じて textContent / style を書き換える
//   - dispose で必ず取り外す（リーク防止）
// ============================================================================

import type { GameStateView } from "./game-state";

export interface Hud {
  /** 毎フレーム呼んで HUD を最新の状態に更新する。 */
  update(state: GameStateView): void;
  dispose(): void;
}

/**
 * HUD を生成し、`host` の上にオーバーレイとして配置します。
 *
 * @param host canvas を保持している要素（ふつうは canvas.parentElement）
 */
export function createHud(host: HTMLElement): Hud {
  // host を相対配置にしておかないと、内側の absolute 子要素が
  // 思わぬ場所に飛んでいってしまう。すでに position が指定されていれば
  // 既存のものを尊重する（重複設定しない）。
  const computed = window.getComputedStyle(host);
  if (computed.position === "static") {
    host.style.position = "relative";
  }

  // ルートコンテナ。pointer-events:none にしておくと、
  // HUD があっても下のキャンバスへのマウス操作は素通しになります。
  // （クリックを受けたい子要素だけ pointer-events:auto を付ける）
  const root = document.createElement("div");
  root.className = "game-hud";
  root.style.cssText = `
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 5;
    color: #f5f6f8;
    font-family: inherit;
    user-select: none;
  `;

  // --- 左上: スコア & ベスト & 速度 ----------------------------------------
  const stats = document.createElement("div");
  stats.style.cssText = `
    position: absolute;
    top: 12px;
    left: 14px;
    padding: 10px 14px;
    background: rgba(11, 13, 18, 0.55);
    border: 1px solid rgba(170, 59, 255, 0.4);
    border-radius: 8px;
    backdrop-filter: blur(4px);
    font-variant-numeric: tabular-nums;
    line-height: 1.4;
    min-width: 160px;
  `;
  stats.innerHTML = `
    <div style="font-size: 11px; color: #8d93a1; letter-spacing: 0.08em;">SCORE</div>
    <div data-hud="score" style="font-size: 22px; font-weight: 700;">0.00</div>
    <div style="margin-top: 6px; font-size: 11px; color: #8d93a1;">
      BEST <span data-hud="best" style="color: #d6d8de;">0.00</span>
      &nbsp;•&nbsp;
      SPEED ×<span data-hud="speed" style="color: #d6d8de;">1.00</span>
    </div>
  `;
  root.appendChild(stats);

  // --- 右上: 操作説明 ------------------------------------------------------
  const help = document.createElement("div");
  help.style.cssText = `
    position: absolute;
    top: 12px;
    right: 14px;
    padding: 8px 12px;
    background: rgba(11, 13, 18, 0.55);
    border: 1px solid #2a3140;
    border-radius: 8px;
    font-size: 12px;
    color: #d6d8de;
    line-height: 1.6;
    text-align: right;
  `;
  help.innerHTML = `
    ←/→ または A/D で移動<br>
    R キーでリスタート
  `;
  root.appendChild(help);

  // --- 中央: Game Over オーバーレイ（最初は非表示） -----------------------
  const overlay = document.createElement("div");
  overlay.style.cssText = `
    position: absolute;
    inset: 0;
    display: none;
    align-items: center;
    justify-content: center;
    background: rgba(10, 12, 20, 0.55);
  `;
  overlay.innerHTML = `
    <div style="
      padding: 28px 40px;
      background: rgba(17, 20, 27, 0.92);
      border: 1px solid #f87171;
      border-radius: 12px;
      text-align: center;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
    ">
      <div style="font-size: 28px; font-weight: 800; color: #f87171; letter-spacing: 0.04em;">
        GAME OVER
      </div>
      <div style="margin-top: 14px; font-size: 14px; color: #8d93a1;">スコア</div>
      <div data-hud="overlay-score" style="font-size: 32px; font-weight: 700; font-variant-numeric: tabular-nums;">0.00</div>
      <div style="margin-top: 8px; font-size: 12px; color: #8d93a1;">
        BEST <span data-hud="overlay-best" style="color: #d6d8de;">0.00</span>
      </div>
      <div style="margin-top: 18px; font-size: 13px; color: #d6d8de;">
        <kbd style="
          padding: 2px 8px;
          border: 1px solid #2a3140;
          border-radius: 4px;
          font-family: inherit;
        ">R</kbd> キーでリスタート
      </div>
    </div>
  `;
  root.appendChild(overlay);

  host.appendChild(root);

  // 子要素はクエリ結果をキャッシュ（毎フレーム querySelector するのは無駄）
  const scoreEl = stats.querySelector<HTMLElement>('[data-hud="score"]');
  const bestEl = stats.querySelector<HTMLElement>('[data-hud="best"]');
  const speedEl = stats.querySelector<HTMLElement>('[data-hud="speed"]');
  const overlayScoreEl = overlay.querySelector<HTMLElement>('[data-hud="overlay-score"]');
  const overlayBestEl = overlay.querySelector<HTMLElement>('[data-hud="overlay-best"]');

  function update(state: GameStateView): void {
    // 数値表示は toFixed(2) で揺れを抑える（毎フレーム末尾桁が変わるとチカチカする）
    if (scoreEl) scoreEl.textContent = state.score.toFixed(2);
    if (bestEl) bestEl.textContent = state.best.toFixed(2);
    if (speedEl) speedEl.textContent = state.speedMultiplier.toFixed(2);

    if (state.phase === "gameover") {
      overlay.style.display = "flex";
      if (overlayScoreEl) overlayScoreEl.textContent = state.score.toFixed(2);
      if (overlayBestEl) overlayBestEl.textContent = state.best.toFixed(2);
    } else {
      overlay.style.display = "none";
    }
  }

  function dispose(): void {
    if (root.parentElement) root.parentElement.removeChild(root);
    // 既存スタイルを上書きしていた場合は元に戻す（必要なら）
    // ここでは host.style.position は触らない（他のサンプルが期待する初期状態を壊さないため）
  }

  return { update, dispose };
}
