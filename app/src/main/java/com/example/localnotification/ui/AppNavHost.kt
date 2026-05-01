package com.example.localnotification.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.localnotification.ui.home.HomeScreen
import com.example.localnotification.ui.step1.Step1Screen
import com.example.localnotification.ui.step2.Step2Screen
import com.example.localnotification.ui.step3.Step3Screen
import com.example.localnotification.ui.step4.Step4Screen
import com.example.localnotification.ui.step5.Step5Screen
import com.example.localnotification.ui.step6.Step6Screen
import com.example.localnotification.ui.step7.Step7Screen
import com.example.localnotification.ui.step8.Step8Screen

/**
 * Navigation Compose によるルート定義。
 *
 * **設計判断**:
 * - 各 Step を独立した Composable 関数として切り出し、画面間の依存を最小化する。
 * - ルート文字列は const にして散逸を防ぐ。
 * - 通知タップによる deeplink は MainActivity が `navigate(route)` を呼ぶことで実現する
 *   (NavGraph の deepLink 機能は学習スコープ外なので使わない)。
 */
object Routes {
    const val HOME = "home"
    const val STEP1 = "step1"
    const val STEP2 = "step2"
    const val STEP3 = "step3"
    const val STEP4 = "step4"
    const val STEP5 = "step5"
    const val STEP6 = "step6"
    const val STEP7 = "step7"
    const val STEP8 = "step8"
}

/**
 * アプリ全体の Navigation グラフを構築するルート Composable。
 *
 * @param navController 上位 ([MainActivity]) で `rememberNavController()` で生成したインスタンス。
 * 通知タップの deep link は Activity がこの navController に対して `navigate(route)` を呼ぶことで実現される。
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onSelectStep = { route -> navController.navigate(route) },
            )
        }
        composable(Routes.STEP1) { Step1Screen(onBack = { navController.popBackStack() }) }
        composable(Routes.STEP2) { Step2Screen(onBack = { navController.popBackStack() }) }
        composable(Routes.STEP3) { Step3Screen(onBack = { navController.popBackStack() }) }
        composable(Routes.STEP4) { Step4Screen(onBack = { navController.popBackStack() }) }
        composable(Routes.STEP5) { Step5Screen(onBack = { navController.popBackStack() }) }
        composable(Routes.STEP6) { Step6Screen(onBack = { navController.popBackStack() }) }
        composable(Routes.STEP7) { Step7Screen(onBack = { navController.popBackStack() }) }
        composable(Routes.STEP8) { Step8Screen(onBack = { navController.popBackStack() }) }
    }
}
