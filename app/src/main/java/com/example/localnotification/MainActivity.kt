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

    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = extractRoute(intent)

        setContent {
            LocalNotificationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val deepLink = pendingDeepLink
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // singleTask で再利用される場合はここに来る。LaunchedEffect 経由で navigate する。
        setIntent(intent)
        pendingDeepLink = extractRoute(intent)
    }

    private fun extractRoute(intent: Intent?): String? =
        intent?.getStringExtra(NotificationBuilders.EXTRA_NAV_ROUTE)
}