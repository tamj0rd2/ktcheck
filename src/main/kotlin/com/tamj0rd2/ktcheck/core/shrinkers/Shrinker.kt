package com.tamj0rd2.ktcheck.core.shrinkers

internal interface Shrinker<T : Comparable<T>, R : ClosedRange<T>> {
    fun defaultShrinkTarget(range: R): T

    fun shrink(
        value: T,
        range: R,
        target: T = defaultShrinkTarget(range),
    ): Sequence<T>
}
