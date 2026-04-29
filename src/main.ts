// ============================================================================
// main.ts — アプリのエントリポイント
// ----------------------------------------------------------------------------
// 役割は「3 つのコンポーネントを配線する」ことだけです。
//
//   - layout    : DOM ツリー（サイドバー / canvas ホスト / メタバー）
//   - runtime   : 共有レンダラ + アニメーションループ
//   - router    : URL ハッシュとサンプル ID の対応
//
// 配線の流れ:
//   ① layout を組み立て、サイドバー上のクリック → router.navigate(id)
//   ② router を起動。ハッシュが変わるたびに activate(id) が呼ばれる
//   ③ activate(id) は registry から Sample を引いて runtime.setSample(...)
// ============================================================================

import { mountLayout } from "./app/layout";
import { HashRouter } from "./app/router";
import { SampleRuntime } from "./core/sample-runtime";
import { defaultSampleId, getSampleById, samples } from "./samples/index";
import "./styles/app.css";

function main(): void {
  // index.html に必ず存在するはずだが、保険として明示的に検証。
  // 早めに失敗させたほうが学習者がエラーに気づきやすいので、
  // 静かに何もしないのではなく throw します。
  const root = document.getElementById("app");
  if (!root) {
    throw new Error("#app root element is missing from index.html");
  }

  // router を先に作ってから layout に渡す（onSelect から navigate を呼ぶため）。
  const router = new HashRouter();
  const layout = mountLayout(
    root,
    samples.map((s) => s.meta),
    (id) => router.navigate(id),
  );

  const runtime = new SampleRuntime(layout.canvasHost);

  /**
   * 指定 ID のサンプルを起動します。
   * 不明な ID が来たときは「デフォルトサンプル」へフォールバックします。
   */
  function activate(id: string): void {
    const sample = getSampleById(id) ?? getSampleById(defaultSampleId);
    if (!sample) return;

    if (sample.meta.id !== id) {
      // 不明 ID をユーザに教えるより、URL を正規化して再度ナビゲートさせる
      // ほうが体験が一貫します。hashchange → activate が再度呼ばれます。
      router.navigate(sample.meta.id);
      return;
    }

    runtime.setSample(sample);
    layout.setActive(sample.meta.id);
    layout.updateMetadata(sample.meta);
    document.title = `${sample.meta.title} | Three.js 学習サンプル`;
  }

  // router.start() は、起動直後にも一度コールバックを呼びます。
  // ハッシュが空ならデフォルトサンプルへ navigate（→ 再度 activate）。
  router.start((id) => {
    if (!id) {
      router.navigate(defaultSampleId);
      return;
    }
    activate(id);
  });
}

main();
