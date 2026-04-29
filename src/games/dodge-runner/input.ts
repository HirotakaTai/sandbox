// ============================================================================
// input.ts — キーボード入力を「状態」として扱えるようにする
// ----------------------------------------------------------------------------
// ゲームでは「キーが押されているあいだ移動し続ける」処理が頻出します。
// `keydown` イベントごとに 1 マスずつ動かすのではなく、
//
//   - keydown で「押されている」フラグを true にする
//   - keyup で false に戻す
//   - 毎フレーム update 内でフラグを見て move する
//
// という形にすると、フレーム速度が変わっても自然な挙動になります。
//
// 学習者がハマりやすいポイント:
//   - keydown の repeat イベントは OS のキーリピート速度に依存するので
//     これを直接「移動量」に使うとカクついた挙動になる
//   - グローバル window にイベントリスナを付けたら dispose で必ず外す
// ============================================================================

/**
 * 毎フレーム読み出される入力の「現在状態」。
 * 値は読み取り専用として扱う（外部から書き換えない）。
 */
export interface InputState {
  /** 左方向へ移動したいか（← または A キー） */
  readonly left: boolean;
  /** 右方向へ移動したいか（→ または D キー） */
  readonly right: boolean;
  /**
   * リスタート要求が立っているか。
   * 一度読んだら自動的に false に戻る「ワンショット」フラグ。
   * （長押しでリスタート連発を防ぐため）
   */
  readonly restartRequested: boolean;
}

/** input ハンドラの戻り値。`dispose` で必ずリスナを解除すること。 */
export interface InputHandle {
  readonly state: InputState;
  /** リスタート要求フラグを消費（読んで false に戻す）。 */
  consumeRestart(): boolean;
  dispose(): void;
}

/**
 * グローバルなキーボード入力をフックして、状態オブジェクトを返します。
 */
export function createInput(): InputHandle {
  // 内部状態は mutable。外部には readonly インターフェース越しに見せる。
  const internal = {
    left: false,
    right: false,
    restartRequested: false,
  };

  function onKeyDown(event: KeyboardEvent): void {
    // 修飾キーが押されているときは無視（ブラウザショートカットと衝突しないように）
    if (event.metaKey || event.ctrlKey || event.altKey) return;

    switch (event.key) {
      case "ArrowLeft":
      case "a":
      case "A":
        internal.left = true;
        // ページがスクロールしないように、矢印キーは preventDefault しておく
        event.preventDefault();
        break;
      case "ArrowRight":
      case "d":
      case "D":
        internal.right = true;
        event.preventDefault();
        break;
      case "r":
      case "R":
        internal.restartRequested = true;
        break;
    }
  }

  function onKeyUp(event: KeyboardEvent): void {
    switch (event.key) {
      case "ArrowLeft":
      case "a":
      case "A":
        internal.left = false;
        break;
      case "ArrowRight":
      case "d":
      case "D":
        internal.right = false;
        break;
    }
  }

  // window に対してリスナを登録する。canvas に focus が当たっていなくても
  // 操作できるようにしたいので、window 全体で受け取るのがシンプル。
  window.addEventListener("keydown", onKeyDown);
  window.addEventListener("keyup", onKeyUp);

  return {
    state: internal,
    consumeRestart(): boolean {
      const requested = internal.restartRequested;
      internal.restartRequested = false; // 一度読んだら必ずリセット
      return requested;
    },
    dispose() {
      // dispose で必ずリスナを外す。これを忘れるとサンプル切替後も
      // バックグラウンドで keydown を拾い続けてメモリリークの原因になる。
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("keyup", onKeyUp);
    },
  };
}
