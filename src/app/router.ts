// ============================================================================
// router.ts — ハッシュベースの簡易ルータ
// ----------------------------------------------------------------------------
// `#/rotating-cube` のように、URL のハッシュ部分でアクティブサンプルを
// 表現します。ハッシュ方式を選んだ理由:
//
//   - サーバ設定（rewrite ルール）が一切不要。静的ホスティングでそのまま動く。
//   - ページリロードしても同じサンプルが開く（学習者が共有しやすい）。
//
// `popstate` ではなく `hashchange` を購読しているのもこのためです。
// ============================================================================

export class HashRouter {
  private listener: ((id: string) => void) | null = null;
  private readonly onHashChange = () => this.emit();

  /**
   * 監視を開始します。`listener` には現在の ID（空文字含む）が渡されます。
   * 起動直後にも 1 回呼ばれるので、初期サンプルの表示はこれをトリガーに
   * 行えます。
   */
  start(listener: (id: string) => void): void {
    this.listener = listener;
    window.addEventListener("hashchange", this.onHashChange);
    this.emit();
  }

  stop(): void {
    window.removeEventListener("hashchange", this.onHashChange);
    this.listener = null;
  }

  /**
   * URL を書き換えます。同じハッシュなら何もしません
   * （ループ防止のためここでガードを入れるのが鉄則）。
   * 書き換え後は hashchange 経由で listener が呼ばれるので、
   * UI 状態の更新パスは常に 1 本に統一できます。
   */
  navigate(id: string): void {
    const target = `#/${id}`;
    if (window.location.hash === target) return;
    window.location.hash = target;
  }

  /** 現在のハッシュから ID 部分だけを取り出します。 */
  private current(): string {
    // 先頭の `#` と `#/` の両方を許容するため、正規表現で削ります。
    const raw = window.location.hash.replace(/^#\/?/, "").trim();
    return raw;
  }

  private emit(): void {
    this.listener?.(this.current());
  }
}
