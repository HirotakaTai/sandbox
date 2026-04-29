// ============================================================================
// samples/index.ts — サンプルレジストリ
// ----------------------------------------------------------------------------
// 「アプリに公開するサンプル」をここで一元管理します。
// 新しいサンプルを追加したいときは:
//   1. `src/samples/<id>/index.ts` を作って Sample を default export
//   2. このファイルで import して `samples` 配列に追加
// するだけで、サイドバーにも URL ルーティングにも反映されます。
//
// 配列の並び順がそのままサイドバーの並び順になります。
// 配列の先頭サンプルが「デフォルトサンプル」として扱われます。
// ============================================================================

import type { Sample } from "../core/types";
import controls from "./controls/index";
import lighting from "./lighting/index";
import postprocessing from "./postprocessing/index";
import rotatingCube from "./rotating-cube/index";
import texture from "./texture/index";

/**
 * UI に公開するサンプルの並び（先頭が初期表示サンプル）。
 */
export const samples: readonly Sample[] = [
  rotatingCube,
  lighting,
  texture,
  controls,
  postprocessing,
];

// ID → Sample の高速ルックアップ用マップ。
// ルータが受け取った URL ハッシュから O(1) で対応サンプルを引きます。
const samplesById = new Map<string, Sample>(samples.map((s) => [s.meta.id, s]));

/** ID からサンプルを引きます。未登録 ID なら undefined。 */
export function getSampleById(id: string): Sample | undefined {
  return samplesById.get(id);
}

/** 起動直後や不明 ID 時のフォールバック先サンプル ID。 */
export const defaultSampleId: string = samples[0].meta.id;
