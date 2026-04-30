package com.example.localnotification.notification

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * NotificationManagerCompat の薄いラッパー。
 *
 * **役割**:
 * - 通知発行の唯一のエントリポイント。すべての画面はここを経由する。
 * - 権限チェックを集約し、`@SuppressLint("MissingPermission")` を 1 箇所に閉じ込める。
 * - テスト時にモック可能な単純な API を提供する。
 *
 * **権限ポリシー**:
 * - 権限が無い場合は黙って捨てる (= notify() がランタイム例外を投げないようにする)。
 *   呼び出し元が事前に [hasPostPermission] で確認するべき。
 *   このラッパーは "fail-safe" として最後の砦になる。
 */
class NotificationPoster(private val context: Context) {

    /**
     * 通知を発行する。権限が無い場合は何もしない (= safe no-op)。
     *
     * @param id   通知 ID。同 ID で再 notify すると更新となる。
     * @param tag  名前空間を分けたい場合に指定。null で OK。
     */
    fun notify(id: Int, notification: Notification, tag: String? = null) {
        if (!hasPostPermission(context)) return
        val nm = NotificationManagerCompat.from(context)
        // 権限を上で確認したので Lint 警告は無効化できる。
        @Suppress("MissingPermission")
        if (tag == null) {
            nm.notify(id, notification)
        } else {
            nm.notify(tag, id, notification)
        }
    }

    /**
     * 指定 ID (とオプションの tag) の通知を消去する。
     *
     * - すでに表示されていない ID を渡しても安全 (例外にはならない)。
     * - グループ通知 (Step 6) では、子をすべて cancel した後、別途 summary も cancel する必要がある。
     */
    fun cancel(id: Int, tag: String? = null) {
        val nm = NotificationManagerCompat.from(context)
        if (tag == null) nm.cancel(id) else nm.cancel(tag, id)
    }

    /** このアプリが発行したすべての通知を一括消去する。デバッグやリセット時に便利。 */
    fun cancelAll() = NotificationManagerCompat.from(context).cancelAll()

    companion object {
        /**
         * POST_NOTIFICATIONS 権限を保持しているかを判定する。
         * - API 32 以下: マニフェスト宣言だけで自動許可されるので常に true。
         * - API 33+   : ランタイム権限の許諾状態を ContextCompat 経由で確認。
         */
        fun hasPostPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }
}
