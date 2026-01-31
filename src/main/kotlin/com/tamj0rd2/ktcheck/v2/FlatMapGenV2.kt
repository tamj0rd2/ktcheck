package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.RandomTree

internal class FlatMapGenV2<T, R>(
    private val gen: GenV2<T>,
    private val fn: (T) -> GenV2<R>,
) : GenV2<R> {
    override fun generate(tree: RandomTree): GenResultV2<R> {
        val (outerValue, outerShrinks) = gen.generate(tree.left)
        val (innerValue, innerShrinks) = fn(outerValue).generate(tree.right)

        return GenResultV2(
            value = innerValue,
            shrinks = shrink(tree, outerShrinks) + innerShrinks
        )
    }

    private fun shrink(
        tree: RandomTree,
        outerShrinks: Sequence<GenResultV2<T>>,
    ): Sequence<GenResultV2<R>> = outerShrinks.map { outer ->
        val (outerValue, outerShrinks) = outer
        val (innerValue, innerShrinks) = fn(outerValue).generate(tree.right)
        GenResultV2(
            value = innerValue,
            shrinks = shrink(tree, outerShrinks) + innerShrinks
        )
    }
}
