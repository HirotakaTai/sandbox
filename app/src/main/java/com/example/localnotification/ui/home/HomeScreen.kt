package com.example.localnotification.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.localnotification.R
import com.example.localnotification.ui.Routes
import com.example.localnotification.ui.common.PermissionStatusCard
import com.example.localnotification.ui.common.rememberNotificationPermissionState

/** ホーム画面で表示する 1 つの Step エントリ。タイトル/説明は文字列リソース ID で保持する。 */
private data class StepEntry(
    val route: String,
    val titleRes: Int,
    val descRes: Int,
)

private val steps = listOf(
    StepEntry(Routes.STEP1, R.string.step1_title, R.string.step1_description),
    StepEntry(Routes.STEP2, R.string.step2_title, R.string.step2_description),
    StepEntry(Routes.STEP3, R.string.step3_title, R.string.step3_description),
    StepEntry(Routes.STEP4, R.string.step4_title, R.string.step4_description),
    StepEntry(Routes.STEP5, R.string.step5_title, R.string.step5_description),
    StepEntry(Routes.STEP6, R.string.step6_title, R.string.step6_description),
    StepEntry(Routes.STEP7, R.string.step7_title, R.string.step7_description),
)

/**
 * ホーム画面。権限ステータスと各 Step へのナビゲーションリンクを表示する。
 *
 * @param onSelectStep Step カードタップ時のコールバック。遷移先の route 文字列を受け取る。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSelectStep: (String) -> Unit) {
    val permission = rememberNotificationPermissionState()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
            )
            PermissionStatusCard(state = permission)
            steps.forEach { entry ->
                StepCard(
                    title = stringResource(entry.titleRes),
                    description = stringResource(entry.descRes),
                    onClick = { onSelectStep(entry.route) },
                )
            }
        }
    }
}

/** 各 Step へのナビゲーションを提供するタップ可能なカード (Material 3 Card)。 */
@Composable
private fun StepCard(title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
