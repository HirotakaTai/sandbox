package com.example.localnotification.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Compose Material 3 の標準的な Theme 定義。
 *
 * **方針**:
 * - Android 12+ の dynamic color (Material You) に対応。
 * - それ以下のバージョンは静的なフォールバック色を使用。
 * - StatusBar の前景色 (アイコン色) を背景に応じて切り替える。
 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFF82B1FF),
    secondary = Color(0xFF80DEEA),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    secondary = Color(0xFF00838F),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
)

/**
 * アプリ全体の Material 3 テーマを適用するルート Composable。
 *
 * @param darkTheme ダークテーマを使うか。デフォルトではシステム設定に合わせる。
 * @param dynamicColor Material You (Android 12+) の壁紙連動色を使うか。
 * @param content テーマ適用下で表示したい Composable ツリー。
 */
@Composable
fun LocalNotificationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        // SideEffect: コンポジションが成功した後に 1 回だけ呼ばれるブロック。
        // Compose の状態を外部システム (ここでは Window) に反映させるために使う。
        // ステータスバーを背景色に揃え、アイコン色をダーク/ライトで切り替える。
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
