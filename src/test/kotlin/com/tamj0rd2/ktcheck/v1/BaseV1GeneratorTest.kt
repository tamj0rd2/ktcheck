package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.core.ProducerTree

internal abstract class BaseV1GeneratorTest {
    fun <T> Gen<T>.generate(tree: ProducerTree): T {
        return (this as GenV1).generate(tree, GenMode.Initial).value
    }

    fun <T> Gen<T>.generateWithShrunkValues(tree: ProducerTree): Pair<T, List<T>> {
        val (value, shrinks) = (this as GenV1).generate(tree, GenMode.Initial)
        return value to shrinks.map { generate(it, GenMode.Shrinking).value }.toList()
    }
}
