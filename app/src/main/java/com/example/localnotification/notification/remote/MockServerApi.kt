package com.example.localnotification.notification.remote

import android.util.Log
import kotlinx.coroutines.delay

/**
 * `onNewToken` 発火時に「自社サーバーへトークンを登録する」処理を模した擬似 API。
 *
 * **本物の実装での注意点**:
 * - HTTPS で送信し、ユーザー認証 (アクセストークン等) と紐付けて保存する。
 * - ネットワーク失敗時はリトライ可能な仕組み (WorkManager の Worker を `Result.retry()` させる) に。
 * - 古いトークン削除は明示的にしない。サーバー側で「同一ユーザーの最新トークンに上書き」するのが定石。
 */
object MockServerApi {

    private const val TAG = "MockServerApi"

    /**
     * トークンをサーバーに登録する擬似処理。実際のネットワーク呼び出しはせず、ログ出力 + 遅延のみ。
     *
     * @return 登録成功なら true。本サンプルでは常に true。
     */
    suspend fun registerToken(token: String): Boolean {
        Log.d(TAG, "POST /api/devices/token  body={token=\"$token\"}")
        // ネットワーク往復を模す
        delay(500)
        Log.d(TAG, "  -> 200 OK")
        return true
    }
}
