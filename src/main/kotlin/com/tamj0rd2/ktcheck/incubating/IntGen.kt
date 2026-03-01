package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker

internal class IntGen(
    private val range: IntRange,
    private val shrinkTarget: Int,
) : GenImpl<Int>() {
    init {
        require(shrinkTarget in range) { "Shrink target $shrinkTarget must be within the range $range" }
    }

    override fun generate(root: RandomTree): GeneratedValue<Int> {
        val value = root.provider.int(range)
        return buildResult(root, value)
    }

    override fun edgeCases(root: RandomTree): List<GeneratedValue<Int>> {
        return setOf(0, range.first, range.last)
            .flatMap { listOf(it, it - 1, it + 1) }
            .filter { it in range }
            .map { buildResult(root = root.withPredeterminedValue(it), value = it) }
    }

    private fun buildResult(
        root: RandomTree,
        value: Int,
    ): GeneratedValue<Int> {
        val shrinks = IntShrinker.shrink(value, range, shrinkTarget).map { root.withPredeterminedValue(it) }

        return GeneratedValue(
            value = value,
            shrinks = shrinks,
            usedTree = root,
        )
    }
}
