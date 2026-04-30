// ルートビルドファイル。プラグインバージョンの集中宣言のみ行い、apply は各モジュールで行う。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}