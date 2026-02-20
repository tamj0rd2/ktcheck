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
        val shrinkTrees = IntShrinker.shrink(value, range, shrinkTarget)
            .map { tree.withProvider(PredeterminedValueProvider(it)) }

        return GenResultV2(
            value = value,
            shrinks = shrinkTrees,
        )
    }

    override fun edgeCases(): List<GenResultV2<Int>> {
        return emptyList()
    }
}
