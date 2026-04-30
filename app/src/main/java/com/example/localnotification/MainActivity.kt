package com.example.localnotification

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.localnotification.notification.NotificationBuilders
import com.example.localnotification.ui.AppNavHost
import com.example.localnotification.ui.theme.LocalNotificationTheme

/**
 * 単一 Activity 構成のエントリポイント。
 *
 * **学習用ポイント**:
 * - ComponentActivity を使用 (AppCompatActivity は不要、Compose だけで完結)。
 * - 通知タップで起動された場合、Intent.extras に [NotificationBuilders.EXTRA_NAV_ROUTE] が
 *   入っているので Navigation の起動 route として利用する。
 * - launchMode="singleTask" + onNewIntent で、既存スタックを保ったまま遷移する。
 */
class MainActivity : ComponentActivity() {

    /**
     * 「通知タップで渡された遷移先 route」の一時保持領域。
     *
     * **これを mutableStateOf にしている理由**:
     * Compose は値の変更を検知して [LaunchedEffect] を再実行する。
     * つまり、`onNewIntent` でこの値を更新すると自動的に navigate が走る。
     * 通常の var だと Compose は変更を検知できないため navigate がトリガーされない。
     */
    private var pendingDeepLink by mutableStateOf<String?>(null)

    /**
     * Activity の初期化。Compose ツリーをセットアップし、
     * 起動時の Intent から deep link route を抽出する。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = extractRoute(intent)

        setContent {
            LocalNotificationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val deepLink = pendingDeepLink
                    // deepLink の値が変わるたびにコルーチンを起動し、navigate を実行する。
                    // LaunchedEffect はコンポジションのライフサイクルに縛り付いているため、
                    // 画面を離れれば自動的にキャンセルされる (メモリリーク防止)。
                    LaunchedEffect(deepLink) {
                        if (!deepLink.isNullOrEmpty()) {
                            navController.navigate(deepLink) {
                                launchSingleTop = true
                            }
                            pendingDeepLink = null // 1 回のみ消費
                        }
                    }
                    AppNavHost(navController = navController)
                }
            }
        }
    }

    /**
     * Activity がすでに生きている状態で Intent を受けたときに呼ばれる。
     *
     * `launchMode="singleTask"` (Manifest で設定済み) により、バックスタックを保ったまま起動される。
     * その際、新しい Intent は onCreate ではなくこちらに渡されるため、
     * ここでも deep link を取り出して pendingDeepLink を更新する必要がある。
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // setIntent: 以降 getIntent() したときに新しい方が返るよう上書きする。
        setIntent(intent)
        pendingDeepLink = extractRoute(intent)
    }

    /** Intent extras から Navigation の遷移先 route 文字列を取り出す。無ければ null。 */
    private fun extractRoute(intent: Intent?): String? =
        intent?.getStringExtra(NotificationBuilders.EXTRA_NAV_ROUTE)
}