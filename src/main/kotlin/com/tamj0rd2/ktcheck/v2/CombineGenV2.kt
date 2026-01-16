package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.ProducerTree

internal class CombineGenV2<T1, T2, R>(
    private val gen1: GenV2<T1>,
    private val gen2: GenV2<T2>,
    private val combine: (T1, T2) -> R,
) : GenV2<R> {
    override fun generate(tree: ProducerTree): GenResultV2<R> {
        // todo: left shrinks are unused.
        val (leftValue, _) = gen1.generate(tree.left)
        val rightResult = gen2.generate(tree.right)

        return GenResultV2(
            value = combine(leftValue, rightResult.value),
            shrinks = rightResult.map { rightValue -> combine(leftValue, rightValue) }.shrinks,
        )
    }
}
