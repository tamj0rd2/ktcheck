package com.tamj0rd2.ktcheck.core.shrinkers

internal interface Shrinker<T : Comparable<T>, R : ClosedRange<T>> {
    fun defaultOrigin(range: R): T

    fun shrink(
        value: T,
        range: R,
        origin: T = defaultOrigin(range),
    ): Sequence<T>
}
