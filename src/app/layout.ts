// ============================================================================
// layout.ts — アプリのレイアウト構築（ヘッダ + サイドバー + メイン領域）
// ----------------------------------------------------------------------------
// このファイルは React などの UI フレームワークを使わず、素の DOM API で
// アプリシェルを組み立てます。Three.js の学習が本題なので、UI 周りの
// 「魔法」を増やさず、何が起きているかが追いやすい構成にしています。
//
// 構成:
//   <div id="app" class="app-shell">
//     <header class="app-header">    … タイトル / サイドバー開閉ボタン
//     <div class="app-body">
//       <aside class="sidebar">       … サンプル一覧（<button> のリスト）
//       <main class="main-panel">     … キャンバスとメタ情報
//
// 注意点:
//   - サイドバー項目は <button> で作る（div + onclick だとキーボード非対応）
//   - 動的に流し込む文字列は escapeHtml() を必ず通す（XSS 対策）
//   - aria-controls / aria-expanded / aria-current でスクリーンリーダ対応
// ============================================================================

import type { SampleMetadata } from "../core/types";

/**
 * GitHub 上のソースコードへのベース URL。
 * メタデータの sourcePath を末尾に連結することで、
 * 「現在表示中のサンプルのソース」へのリンクを生成します。
 */
const SOURCE_BASE_URL = "https://github.com/HirotakaTai/sandbox/blob/threejs/threejs-example/";

/**
 * `mountLayout()` が返す操作ハンドル。アプリの他の部分が
 * UI を書き換えるための「窓口」として使います。
 */
export interface LayoutHandles {
  /** Three.js の canvas をぶら下げるホスト要素。 */
  readonly canvasHost: HTMLElement;
  /** サイドバーで指定した ID をアクティブ表示にする。 */
  readonly setActive: (id: string) => void;
  /** メタ情報バー（タイトル / 説明 / ソースリンク）を更新する。 */
  readonly updateMetadata: (meta: SampleMetadata) => void;
}

/**
 * アプリシェルを組み立てて、操作用ハンドルを返します。
 *
 * @param root        マウント先（通常は `#app` の DOM 要素）
 * @param samples     サイドバーに並べるサンプルのメタデータ一覧
 * @param onSelect    サンプル選択時のコールバック（router.navigate を呼ぶ）
 */
export function mountLayout(
  root: HTMLElement,
  samples: readonly SampleMetadata[],
  onSelect: (id: string) => void,
): LayoutHandles {
  // 既存の中身（Vite の初期スキャフォールドなど）をクリアして、
  // クラスを付与してスタイルの足場を作ります。
  root.innerHTML = "";
  root.classList.add("app-shell");

  const sidebarToggleId = "sidebar-toggle";
  const sidebarId = "sample-sidebar";

  // --- ヘッダ ---------------------------------------------------------------
  // サイドバー開閉ボタンに aria-controls / aria-expanded を付けることで、
  // スクリーンリーダ利用者にも「これは何を制御するボタンか」が伝わります。
  const header = document.createElement("header");
  header.className = "app-header";
  header.innerHTML = `
    <button
      id="${sidebarToggleId}"
      class="sidebar-toggle"
      type="button"
      aria-controls="${sidebarId}"
      aria-expanded="true"
      aria-label="サンプル一覧の表示切替"
    >
      <span class="sidebar-toggle__icon" aria-hidden="true"></span>
    </button>
    <h1 class="app-title">Three.js 学習サンプル</h1>
    <a class="app-repo-link" href="${SOURCE_BASE_URL}" target="_blank" rel="noreferrer noopener">
      リポジトリを開く
    </a>
  `;

  const body = document.createElement("div");
  body.className = "app-body";

  // --- サイドバー -----------------------------------------------------------
  const sidebar = document.createElement("aside");
  sidebar.id = sidebarId;
  sidebar.className = "sidebar";
  sidebar.setAttribute("aria-label", "サンプル一覧");

  const list = document.createElement("ul");
  list.className = "sample-list";
  list.setAttribute("role", "list");

  // 各サンプル用のボタンを構築。
  // ボタン要素は後で active 表示の付け外しに使うため、Map で保持しておきます。
  const buttons = new Map<string, HTMLButtonElement>();
  for (const meta of samples) {
    const item = document.createElement("li");
    item.className = "sample-list__item";
    const button = document.createElement("button");
    button.type = "button";
    button.className = "sample-list__button";
    button.dataset.sampleId = meta.id;
    // メタデータ由来の文字列は必ず escapeHtml で無害化してから innerHTML に渡す。
    button.innerHTML = `
      <span class="sample-list__title">${escapeHtml(meta.title)}</span>
      <span class="sample-list__summary">${escapeHtml(meta.summary)}</span>
      <span class="sample-list__tags">
        <span class="badge badge--difficulty badge--${meta.difficulty}">${difficultyLabel(meta.difficulty)}</span>
        ${meta.tags.map((t) => `<span class="badge">${escapeHtml(t)}</span>`).join("")}
      </span>
    `;
    button.addEventListener("click", () => onSelect(meta.id));
    item.appendChild(button);
    list.appendChild(item);
    buttons.set(meta.id, button);
  }
  sidebar.appendChild(list);

  // --- メイン領域 -----------------------------------------------------------
  const main = document.createElement("main");
  main.className = "main-panel";

  // キャンバス本体ではなく「ホスト要素」を作る。
  // 実際の <canvas> はランタイム側 (SampleRuntime) が生成して append します。
  const canvasHost = document.createElement("div");
  canvasHost.className = "canvas-host";
  canvasHost.setAttribute("aria-label", "サンプル描画エリア");

  // 下部のメタ情報バー。タイトル / 説明 / ソースリンク / タグを表示します。
  const metaBar = document.createElement("section");
  metaBar.className = "meta-bar";
  metaBar.innerHTML = `
    <div class="meta-bar__main">
      <h2 class="meta-bar__title" data-meta="title"></h2>
      <p class="meta-bar__description" data-meta="description"></p>
    </div>
    <div class="meta-bar__aside">
      <a class="meta-bar__source" data-meta="source" href="#" target="_blank" rel="noreferrer noopener">
        ソースコードを見る →
      </a>
      <div class="meta-bar__tags" data-meta="tags"></div>
    </div>
  `;

  main.appendChild(canvasHost);
  main.appendChild(metaBar);

  body.appendChild(sidebar);
  body.appendChild(main);

  root.appendChild(header);
  root.appendChild(body);

  // --- サイドバーの開閉処理 -------------------------------------------------
  const toggle = header.querySelector<HTMLButtonElement>(`#${sidebarToggleId}`);

  // モバイル幅で初回ロードしたとき、サイドバーがキャンバスを覆ってしまうと
  // 学習者の体験を損なうので、最初から閉じておきます。
  const isMobile = window.matchMedia("(max-width: 768px)").matches;
  if (isMobile) {
    root.classList.add("app-shell--sidebar-collapsed");
    toggle?.setAttribute("aria-expanded", "false");
  }

  toggle?.addEventListener("click", () => {
    const collapsed = root.classList.toggle("app-shell--sidebar-collapsed");
    toggle.setAttribute("aria-expanded", collapsed ? "false" : "true");
  });

  // モバイルでサンプルを選んだら、オーバーレイサイドバーを自動で閉じる。
  list.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof Element)) return;
    if (!target.closest(".sample-list__button")) return;
    if (window.matchMedia("(max-width: 768px)").matches) {
      root.classList.add("app-shell--sidebar-collapsed");
      toggle?.setAttribute("aria-expanded", "false");
    }
  });

  // メタバーの内側要素は更新時に何度も触るので、参照をキャッシュしておく。
  const titleEl = metaBar.querySelector<HTMLElement>('[data-meta="title"]');
  const descEl = metaBar.querySelector<HTMLElement>('[data-meta="description"]');
  const sourceEl = metaBar.querySelector<HTMLAnchorElement>('[data-meta="source"]');
  const tagsEl = metaBar.querySelector<HTMLElement>('[data-meta="tags"]');

  /**
   * サイドバーで「現在のサンプル」をハイライトします。
   * `aria-current="page"` を付与することで、スクリーンリーダが
   * 「現在のページ」とアナウンスしてくれます。
   */
  function setActive(id: string): void {
    for (const [bid, btn] of buttons) {
      const isActive = bid === id;
      btn.classList.toggle("is-active", isActive);
      if (isActive) {
        btn.setAttribute("aria-current", "page");
      } else {
        btn.removeAttribute("aria-current");
      }
    }
  }

  /**
   * メタ情報バーを更新します。サンプル切替のたびに呼ばれます。
   */
  function updateMetadata(meta: SampleMetadata): void {
    if (titleEl) titleEl.textContent = meta.title;
    if (descEl) descEl.textContent = meta.description;
    if (sourceEl) sourceEl.href = `${SOURCE_BASE_URL}${meta.sourcePath}`;
    if (tagsEl) {
      tagsEl.innerHTML = `
        <span class="badge badge--difficulty badge--${meta.difficulty}">${difficultyLabel(meta.difficulty)}</span>
        ${meta.tags.map((t) => `<span class="badge">${escapeHtml(t)}</span>`).join("")}
      `;
    }
  }

  return { canvasHost, setActive, updateMetadata };
}

/** 難易度コードを日本語ラベルに変換します。 */
function difficultyLabel(d: SampleMetadata["difficulty"]): string {
  switch (d) {
    case "beginner":
      return "初級";
    case "intermediate":
      return "中級";
    case "advanced":
      return "上級";
  }
}

/**
 * 文字列を HTML エンティティに変換します（最小限の XSS 対策）。
 * メタデータは現状すべて自前で書いていますが、将来 user input を
 * 受け付けるようになっても安全に動かすため、最初から通しておきます。
 */
function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}
