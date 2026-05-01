package com.example.localnotification.notification.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FCM の `subscribeToTopic` / `unsubscribeFromTopic` を学習用にモックしたレジストリ。
 *
 * **本物の FCM での挙動**:
 * - トピック購読は **クライアント単独で完結** する (サーバーから何かを送る必要はない)。
 * - 購読状態は端末側 + Firebase バックエンドが保持する。
 * - サーバーから「トピックに対して送信」すると、購読中の全端末に届く。
 * - 一度購読すれば再起動後も維持される (Firebase が永続化)。
 *
 * 本実装は SharedPreferences に購読中トピックを保存するだけのシンプルなモック。
 */
class TopicRegistry(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _topics = MutableStateFlow(loadTopics())
    /** UI が observe するための購読中トピック一覧。 */
    val topics: StateFlow<Set<String>> = _topics.asStateFlow()

    /** 指定トピックを購読する (重複呼び出しは no-op)。 */
    fun subscribe(topic: String) {
        val updated = _topics.value + topic
        persist(updated)
        Log.d(TAG, "subscribed: $topic  (total=${updated.size})")
    }

    /** 指定トピックの購読を解除する (未購読時は no-op)。 */
    fun unsubscribe(topic: String) {
        val updated = _topics.value - topic
        persist(updated)
        Log.d(TAG, "unsubscribed: $topic  (total=${updated.size})")
    }

    /** 受信メッセージが現在の購読トピックに属するかを判定する補助メソッド。 */
    fun isSubscribed(topic: String): Boolean = topic in _topics.value

    private fun persist(topics: Set<String>) {
        prefs.edit().putStringSet(KEY_TOPICS, topics).apply()
        _topics.value = topics
    }

    private fun loadTopics(): Set<String> =
        prefs.getStringSet(KEY_TOPICS, emptySet())?.toSet() ?: emptySet()

    companion object {
        private const val TAG = "TopicRegistry"
        private const val PREFS_NAME = "remote_topic_prefs"
        private const val KEY_TOPICS = "subscribed_topics"
    }
}
