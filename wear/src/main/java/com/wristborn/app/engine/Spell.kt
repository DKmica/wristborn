package com.wristborn.app.engine

import com.wristborn.app.sensors.GestureType

data class Spell(
    val sigil: List<SigilToken>,
    val element: Element,
    val form: FormType,
    val release: GestureType,
    val timestampMs: Long
)
