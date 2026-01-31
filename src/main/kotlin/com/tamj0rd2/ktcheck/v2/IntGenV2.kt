package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.RandomTree
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker.shrink
import kotlin.random.nextInt

internal class IntGenV2(
    private val range: IntRange,
    private val shrinkingTarget: Int = IntShrinker.defaultOrigin(range),
) : GenV2<Int> {
    override fun generate(tree: RandomTree): GenResultV2<Int> {
        val value = tree.random.nextInt(range)
        return buildResult(value)
    }

    override fun edgeCases(): List<GenResultV2<Int>> {
        return listOf(0, shrinkingTarget, range.first, range.last)
            .flatMap { listOf(it, it + 1, it - 1) }
            .distinct()
            .filter { it in range }
            .map { buildResult(it) }
    }

    private fun buildResult(value: Int): GenResultV2<Int> = GenResultV2(
        value = value,
        shrinks = shrink(value, range, shrinkingTarget).map { buildResult(it) }
    )
}
