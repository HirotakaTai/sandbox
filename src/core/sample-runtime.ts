// ============================================================================
// sample-runtime.ts — サンプル共通のランタイム
// ----------------------------------------------------------------------------
// このクラスは、すべてのサンプルで共通して必要となる以下の責務を
// 1 か所にまとめています。
//
//   1. WebGLRenderer の生成と canvas の DOM 取り付け
//   2. requestAnimationFrame による単一のアニメーションループ
//   3. ResizeObserver によるキャンバスサイズの追従
//   4. アクティブサンプルの差し替え / 後片付け
//
// 各サンプルがそれぞれ独自に RAF を回したり renderer を作ったりすると、
// 切り替え時に GPU リソースが残り続けて不安定になりがちです。
// ここで一元管理することで、サンプル側は「描く対象（Scene/Camera）」と
// 「片付け方（dispose）」だけに集中できます。
// ============================================================================

import { WebGLRenderer } from "three";
import type { Sample, SampleContext, SampleHandle } from "./types";

export class SampleRuntime {
  private readonly container: HTMLElement;
  private readonly renderer: WebGLRenderer;
  private readonly canvas: HTMLCanvasElement;
  private readonly resizeObserver: ResizeObserver;

  private active: { sample: Sample; handle: SampleHandle } | null = null;
  private rafId: number | null = null;
  private lastTime = 0;
  private startTime = 0;
  private width = 0;
  private height = 0;

  /**
   * @param container キャンバスを差し込む親要素。
   * この要素のサイズを CSS で決め、それに追従する形で renderer を
   * リサイズします（canvas に幅/高さを直書きしません）。
   */
  constructor(container: HTMLElement) {
    this.container = container;

    // antialias: true でジャギーを抑える。alpha: false にしておくと
    // 背景ピクセルが不透明になり、ブレンドコストを抑えられます。
    this.renderer = new WebGLRenderer({ antialias: true, alpha: false });

    // Retina ディスプレイで 3 倍解像度などにすると塗りつぶしコストが急増する
    // ので、devicePixelRatio は 2 で頭打ちにするのが定石です。
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    this.canvas = this.renderer.domElement;
    this.canvas.classList.add("sample-canvas");
    this.container.appendChild(this.canvas);

    // 初期サイズはコンテナのレイアウト後の大きさから取得します。
    const initialRect = this.container.getBoundingClientRect();
    this.width = Math.max(1, Math.floor(initialRect.width));
    this.height = Math.max(1, Math.floor(initialRect.height));

    // 第 3 引数 false: canvas 要素の style.width / style.height を上書きしない
    // （CSS 側でレイアウトを完全にコントロールしたいので）。
    this.renderer.setSize(this.width, this.height, false);

    // window.resize ではなく ResizeObserver を使うことで、
    // 「サイドバー開閉でメイン領域だけリサイズされた」ようなケースも検知できます。
    this.resizeObserver = new ResizeObserver((entries) => {
      const entry = entries[0];
      if (!entry) return;
      const { width, height } = entry.contentRect;
      this.handleResize(Math.max(1, Math.floor(width)), Math.max(1, Math.floor(height)));
    });
    this.resizeObserver.observe(this.container);
  }

  /**
   * サンプルを差し替えます。前のサンプルがあれば必ず dispose します。
   */
  setSample(sample: Sample): void {
    this.disposeActive();

    const context: SampleContext = {
      renderer: this.renderer,
      canvas: this.canvas,
      width: this.width,
      height: this.height,
    };

    const handle = sample.init(context);
    this.active = { sample, handle };

    // 初期化直後にリサイズフックを 1 回呼んでおくと、
    // 「コンテナサイズと不一致のまま 1 フレーム描画される」ような
    // 軽い乱れを防げます。
    handle.resize?.(this.width, this.height);

    if (this.rafId === null) this.start();
  }

  /**
   * ランタイム自体を破棄します。アプリ終了時にだけ呼びます。
   */
  destroy(): void {
    this.stop();
    this.disposeActive();
    this.resizeObserver.disconnect();
    this.renderer.dispose();
    if (this.canvas.parentElement === this.container) {
      this.container.removeChild(this.canvas);
    }
  }

  /** RAF ループを開始します。すでに動いていれば何もしません。 */
  private start(): void {
    this.startTime = performance.now();
    this.lastTime = this.startTime;
    const tick = (now: number) => {
      // 次のフレームを先に予約してから描画する書き方。
      // こうしておくと、描画中に例外が出ても次のフレームは走り続けます
      // （無限フリーズになりにくい）。
      this.rafId = requestAnimationFrame(tick);
      const delta = (now - this.lastTime) / 1000;
      const elapsed = (now - this.startTime) / 1000;
      this.lastTime = now;
      this.renderFrame(delta, elapsed);
    };
    this.rafId = requestAnimationFrame(tick);
  }

  private stop(): void {
    if (this.rafId !== null) {
      cancelAnimationFrame(this.rafId);
      this.rafId = null;
    }
  }

  /**
   * 1 フレーム分の処理。
   *   1. サンプルの `update` を呼ぶ（あれば）
   *   2. `customRender` があればそれを呼び、無ければ renderer.render する
   */
  private renderFrame(delta: number, elapsed: number): void {
    if (!this.active) return;
    const { handle } = this.active;
    const info = { delta, elapsed, width: this.width, height: this.height };
    handle.update?.(info);
    if (handle.customRender) {
      handle.customRender(info);
    } else {
      this.renderer.render(handle.scene, handle.camera);
    }
  }

  /**
   * リサイズ通知を受けたときの処理。
   * camera.aspect の更新と camera.updateProjectionMatrix() を忘れると、
   * 画面が縦長/横長に歪むので注意。
   */
  private handleResize(width: number, height: number): void {
    if (width === this.width && height === this.height) return;
    this.width = width;
    this.height = height;
    this.renderer.setSize(width, height, false);
    if (this.active) {
      const { camera } = this.active.handle;
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      this.active.handle.resize?.(width, height);
    }
  }

  /** 現在のサンプルを安全に dispose します。 */
  private disposeActive(): void {
    if (!this.active) return;
    try {
      this.active.handle.dispose();
    } catch (error) {
      // 一つのサンプルの dispose が失敗しても、他のサンプルへの
      // 切り替えが完全に止まらないように try/catch でガードします。
      console.error("Sample disposal failed:", error);
    }
    this.active = null;
  }
}
