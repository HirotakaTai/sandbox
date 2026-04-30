package com.example.localnotification.notification

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * BroadcastReceiver や Worker から発生したイベントを ViewModel / UI に届けるための
 * プロセス内グローバル SharedFlow。
 *
 * **設計判断**:
 * - 学習プロジェクトのため DI を導入していない。プロセス内で 1 インスタンスを
 *   保つ最もシンプルな方法として `object` を採用している。
 * - `extraBufferCapacity = 16` + `DROP_OLDEST` でアプリが裏に回っているときに
 *   イベントが溜まりすぎないようにしている。
 * - 値型 (sealed interface) として表現することで、消費側で `when` の網羅性を
 *   コンパイラに強制できる。
 */
object NotificationEvents {

    sealed interface Event {
        data class MarkedRead(val notificationId: Int) : Event
        data class Replied(val notificationId: Int, val text: String) : Event
    }

    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Event> = _events.asSharedFlow()

    fun emit(event: Event) {
        // tryEmit は同期で送れる (suspending API を呼べない場所からの emit に必須)。
        _events.tryEmit(event)
    }
}
