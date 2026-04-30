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

    /** UI 側に伝える出来事の型。`when` で網羅性チェックが効くよう sealed interface にしている。 */
    sealed interface Event {
        /** 既読アクションが押された (Step 4)。 */
        data class MarkedRead(val notificationId: Int) : Event
        /** RemoteInput で返信が送信された (Step 4)。 */
        data class Replied(val notificationId: Int, val text: String) : Event
    }

    // replay=0 → 後から collect しても過去のイベントは流れない (一過性のシグナル)。
    // extraBufferCapacity=16 → 観測者がいなくても 16 件まではバッファされる。
    // DROP_OLDEST → 溢れたら古い方を捨てる (UI 不在時にメモリが膨らむのを防ぐ)。
    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** UI 側で `events.collect { }` するための公開 Flow。書き込みはできない (asSharedFlow 経由)。 */
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /**
     * イベントを送信する。BroadcastReceiver や Worker など suspend 関数を呼べない場所からも安全に呼べる。
     *
     * tryEmit を使う理由: emit() は suspend 関数なので非 suspend な onReceive() からは呼べない。
     * tryEmit は同期メソッドで、バッファに余裕があれば即座に送り、なければ false を返す。
     */
    fun emit(event: Event) {
        _events.tryEmit(event)
    }
}
