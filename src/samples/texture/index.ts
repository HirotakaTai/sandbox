// ============================================================================
// texture — テクスチャマッピングの基本サンプル
// ----------------------------------------------------------------------------
// 画像ファイルを別途用意せず、HTMLCanvasElement に図形を描画して、
// それをそのままテクスチャとして使います（CanvasTexture）。
//
// テクスチャ周りで初学者がつまずきやすいポイント:
//   - 色空間（colorSpace）を SRGBColorSpace にしないと、色が暗く / 鈍く見える
//   - wrapS / wrapT と repeat を組み合わせて UV の繰り返しが決まる
//   - anisotropy を上げると、斜めから見たときのにじみが減る
// ============================================================================

import {
  BoxGeometry,
  CanvasTexture,
  Color,
  Mesh,
  MeshStandardMaterial,
  PerspectiveCamera,
  PointLight,
  RepeatWrapping,
  Scene,
  SRGBColorSpace,
} from "three";
import type { Sample, SampleContext, SampleHandle } from "../../core/types";

const meta = {
  id: "texture",
  title: "テクスチャマッピング",
  summary: "CanvasTexture を使ってチェッカー柄を立方体に貼り付けます。",
  description:
    "外部画像を読み込まずに HTMLCanvasElement から直接 CanvasTexture を生成します。wrapS / wrapT と repeat を変更すると貼り付け方が変わるので、コードを書き換えて挙動を確認してみてください。",
  tags: ["texture", "uv", "canvas"],
  difficulty: "intermediate",
  sourcePath: "src/samples/texture/index.ts",
  docPath: "docs/samples/texture.md",
} as const;

/**
 * チェッカー柄を描いた `<canvas>` を返します。
 * これを `CanvasTexture` のソースに渡すと、画像ファイルを使わずに
 * テクスチャを作れます。学習用途に便利なテクニックです。
 */
function createCheckerCanvas(size: number, cells: number): HTMLCanvasElement {
  const canvas = document.createElement("canvas");
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext("2d");
  if (!ctx) {
    // ヘッドレス環境などで 2D コンテキストが取れないケースに備える。
    // 本番アプリでは UI 上のフォールバックを用意するのが望ましい。
    throw new Error("2D canvas context is not available");
  }

  // セル単位でチェッカー柄を塗る
  const cellSize = size / cells;
  for (let y = 0; y < cells; y += 1) {
    for (let x = 0; x < cells; x += 1) {
      const isDark = (x + y) % 2 === 0;
      ctx.fillStyle = isDark ? "#1f2230" : "#e7e9ef";
      ctx.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
    }
  }

  // 中央に "UV" の文字を入れて、テクスチャの向き（上下/左右の反転や
  // 繰り返し方向）が直感的に分かるようにします。
  ctx.fillStyle = "#aa3bff";
  ctx.font = `bold ${Math.floor(cellSize * 0.55)}px system-ui, sans-serif`;
  ctx.textAlign = "center";
  ctx.textBaseline = "middle";
  ctx.fillText("UV", size / 2, size / 2);
  return canvas;
}

function init(context: SampleContext): SampleHandle {
  const scene = new Scene();
  scene.background = new Color(0x0b0d12);

  const camera = new PerspectiveCamera(45, context.width / context.height, 0.1, 100);
  camera.position.set(2.6, 1.8, 3);
  camera.lookAt(0, 0, 0);

  // --- テクスチャを作る -----------------------------------------------------
  const checkerCanvas = createCheckerCanvas(512, 8);
  const texture = new CanvasTexture(checkerCanvas);

  // 色を「画面で見たまま」表示するためのおまじない。
  // sRGB を指定しないと、ブラウザでは合っているように見える色が
  // Three.js 内では暗く扱われ、結果として鈍い色合いになります。
  texture.colorSpace = SRGBColorSpace;

  // wrapS / wrapT は U / V それぞれの「はみ出した時の挙動」。
  // RepeatWrapping にしておくと、repeat の値だけタイル状に繰り返します。
  texture.wrapS = RepeatWrapping;
  texture.wrapT = RepeatWrapping;
  texture.repeat.set(2, 2); // U/V 方向に 2 回ずつ繰り返す

  // 異方性フィルタリング（anisotropy）を最大にすると、斜めから見た時の
  // テクスチャがクッキリ見えます。GPU が対応する最大値はハードによって
  // 異なるので、capabilities から取得して渡すのが安全です。
  texture.anisotropy = context.renderer.capabilities.getMaxAnisotropy();

  // --- 立方体に貼り付ける ---------------------------------------------------
  const geometry = new BoxGeometry(1.4, 1.4, 1.4);
  const material = new MeshStandardMaterial({
    map: texture, // ← ここでテクスチャをマテリアルに割り当てる
    roughness: 0.55,
    metalness: 0.05,
  });
  const cube = new Mesh(geometry, material);
  scene.add(cube);

  // テクスチャの色を見るためにライトを 1 つ置きます
  const point = new PointLight(0xffffff, 2.2, 10, 1.6);
  point.position.set(2.5, 2.5, 2.5);
  scene.add(point);

  return {
    scene,
    camera,
    update({ delta }) {
      cube.rotation.y += delta * 0.5;
      cube.rotation.x += delta * 0.2;
    },
    dispose() {
      // Texture も dispose 必須。GPU 上のテクスチャメモリが解放されます。
      // 内部の <canvas> 要素は GC 任せで OK（参照を持ち続けなければ
      // 自動的に回収されます）。
      geometry.dispose();
      material.dispose();
      texture.dispose();
      scene.remove(cube, point);
    },
  };
}

const sample: Sample = { meta, init };
export default sample;
