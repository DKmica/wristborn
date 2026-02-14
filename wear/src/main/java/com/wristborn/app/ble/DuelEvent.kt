package com.wristborn.app.ble

import com.wristborn.app.engine.Element
import com.wristborn.app.engine.FormType

data class DuelEvent(
    val type: EventType,
    val timestampDelta: Long,
    val spellHash: Int = 0,
    val damage: Int = 0,
    val element: Element? = null,
    val form: FormType? = null
)

enum class EventType {
    HANDSHAKE_REQ,
    HANDSHAKE_ACK,
    MATCH_START,
    SPELL_CAST,
    MATCH_END,
    HEARTBEAT
}
