package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker

internal class IntGen(
    private val range: IntRange,
    private val shrinkTarget: Int,
) : GenImpl<Int>() {
    init {
        require(shrinkTarget in range) { "Shrink target $shrinkTarget must be within the range $range" }
    }

    override fun generate(tree: RandomTree): GenResultV2<Int> {
        val value = tree.provider.int(range)
        return buildResult(tree, value)
    }

    override fun edgeCases(tree: RandomTree): List<GenResultV2<Int>> {
        return setOf(0, range.first, range.last)
            .flatMap { listOf(it, it - 1, it + 1) }
            .filter { it in range }
            .map { buildResult(tree, it) }
    }

    private fun buildResult(
        tree: RandomTree,
        value: Int,
    ): GenResultV2<Int> {
        val shrinkTrees = IntShrinker.shrink(value, range, shrinkTarget)
            .map { tree.withProvider(PredeterminedValueProvider(it)) }

        return GenResultV2(
            value = value,
            tree = tree,
            shrinks = shrinkTrees,
        )
    }
}
