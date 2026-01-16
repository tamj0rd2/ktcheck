package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.ProducerTree

internal class FlatMapGenV2<T, R>(
    private val gen: GenV2<T>,
    private val fn: (T) -> GenV2<R>,
) : GenV2<R> {
    override fun generate(tree: ProducerTree): GenResultV2<R> {
        // todo: outer shrinks are unused.
        val (outerValue, _) = gen.generate(tree.left)
        val (innerValue, innerShrinks) = fn(outerValue).generate(tree.right)

        return GenResultV2(
            value = innerValue,
            shrinks = innerShrinks
        )
    }
}
