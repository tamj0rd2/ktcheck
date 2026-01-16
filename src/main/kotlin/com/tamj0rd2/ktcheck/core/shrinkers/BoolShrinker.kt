package com.tamj0rd2.ktcheck.core.shrinkers

internal object BoolShrinker {
    fun defaultOrigin(): Boolean = false

    fun shrink(value: Boolean, origin: Boolean): Sequence<Boolean> = when (value) {
        origin -> emptySequence()
        true -> sequenceOf(false)
        false -> sequenceOf(true)
    }
}
