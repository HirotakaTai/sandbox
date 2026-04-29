// ============================================================================
// types.ts — サンプル間で共有する型定義
// ----------------------------------------------------------------------------
// このファイルは「ランタイム」と「各サンプル」のあいだの "契約" です。
// ここで定義されたインターフェースを満たしてさえいれば、
// 新しいサンプルを足してもランタイムや UI を一切変更しなくて済みます。
//
// 学習用サンプル集としては、この境界を明示するのが特に大事です。
// 「どこまでがランタイムの責務で、どこからがサンプルの責務か」が
// 一目で分かるようになります。
// ============================================================================

import type { PerspectiveCamera, Scene, WebGLRenderer } from "three";

/**
 * 難易度ラベル。サイドバーのバッジ表示や、学習者がサンプルを
 * 選ぶときの目安として使います。
 */
export type SampleDifficulty = "beginner" | "intermediate" | "advanced";

/**
 * サンプルのメタデータ（静的情報）。
 *
 * - `id` は URL（`#/<id>`）の一部になるため、一度公開した ID は変更しないこと。
 * - `sourcePath` は `src/samples/...` 配下の入口ファイルを指す必要があります
 *   （UI が GitHub 上のソースリンクを組み立てるのに使われます）。
 */
export interface SampleMetadata {
  /** kebab-case の安定 ID。URL とレジストリで使われます。 */
  readonly id: string;
  /** サイドバーに表示する人間向けの名前。 */
  readonly title: string;
  /** サイドバー / メタバーに出す 1 行サマリ。 */
  readonly summary: string;
  /** アクティブサンプル時に表示する、学習者向けの少し長めの説明。 */
  readonly description: string;
  /** バッジとして並べるタグ。後々グループ化するのに便利です。 */
  readonly tags: readonly string[];
  /** 難易度。 */
  readonly difficulty: SampleDifficulty;
  /** ワークスペースルートからのソースファイルの相対パス。 */
  readonly sourcePath: string;
  /** 学習メモの相対パス（任意）。 */
  readonly docPath?: string;
}

/**
 * サンプル初期化時にランタイムから渡される実行コンテキスト。
 *
 * サンプルは独自に renderer を作ったり、canvas を DOM に追加したり、
 * requestAnimationFrame を回したりしてはいけません。
 * これらはすべてランタイムの責務です（サンプル切替時に予測可能な
 * 振る舞いを保つため）。
 */
export interface SampleContext {
  /** ランタイムが所有する共有 WebGLRenderer。 */
  readonly renderer: WebGLRenderer;
  /** ランタイムが管理しているキャンバス DOM 要素。 */
  readonly canvas: HTMLCanvasElement;
  /** 初期キャンバス幅（CSS ピクセル）。 */
  readonly width: number;
  /** 初期キャンバス高さ（CSS ピクセル）。 */
  readonly height: number;
}

/**
 * 毎フレームのアニメーション情報。`update` / `customRender` に渡されます。
 */
export interface SampleFrameInfo {
  /** 前フレームからの経過秒数。回転量などに掛けると FPS 非依存になります。 */
  readonly delta: number;
  /** サンプル開始からの経過秒数。sin/cos 系のアニメに便利。 */
  readonly elapsed: number;
  /** 現在のキャンバス幅（CSS ピクセル）。 */
  readonly width: number;
  /** 現在のキャンバス高さ（CSS ピクセル）。 */
  readonly height: number;
}

/**
 * サンプルの `init` が返すハンドル。
 * ランタイムがこのハンドル経由で `update` / `customRender` / `resize` / `dispose`
 * を駆動します。
 *
 * `dispose` ではサンプルが作成した全ての Three.js リソース
 * （geometry / material / texture / controls / render target /
 *  postprocessing pass / event listener など）を必ず解放してください。
 * 解放を忘れると、サンプル切替のたびに GPU メモリが積み上がっていきます。
 */
export interface SampleHandle {
  /** メインのシーン。`customRender` が無ければランタイムがこれを描画します。 */
  readonly scene: Scene;
  /** メインのカメラ。リサイズ時に aspect 更新の対象になります。 */
  readonly camera: PerspectiveCamera;
  /**
   * 毎フレームのアニメーション更新フック（任意）。
   * 描画ではなく「状態の更新」だけを書く場所です。
   */
  update?: (info: SampleFrameInfo) => void;
  /**
   * 独自レンダリングフック（任意）。
   * EffectComposer など、自前のパイプラインで描画したい場合に実装します。
   * これが提供されていると、ランタイムはデフォルトの `renderer.render()` を
   * 呼ばず、こちらに描画を完全に委ねます。
   */
  customRender?: (info: SampleFrameInfo) => void;
  /**
   * リサイズフック（任意）。
   * ランタイム側で `renderer.setSize` と `camera.aspect` の更新は済ませた
   * 状態で呼ばれるので、ここでは「サンプル固有のリサイズ」だけを書きます
   * （例: composer.setSize, bloom.setSize）。
   */
  resize?: (width: number, height: number) => void;
  /**
   * 後片付け。サンプル切替やページ遷移で必ず呼ばれます。
   * 作成した Three.js リソースを漏れなく解放してください。
   */
  dispose: () => void;
}

/**
 * 1 つのサンプルモジュール。
 * `src/samples/<id>/index.ts` の default export がこの型を満たします。
 */
export interface Sample {
  readonly meta: SampleMetadata;
  /** シーンなどを構築してハンドルを返す関数。 */
  readonly init: (context: SampleContext) => SampleHandle;
}
