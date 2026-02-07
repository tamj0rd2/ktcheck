package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker.shrink
import kotlin.random.nextInt

internal class IntGen(
    private val range: IntRange,
    private val shrinkTarget: Int,
) : GenImpl<Int>() {
    init {
        require(shrinkTarget in range) { "Shrink target must be within the specified range." }
    }

    override fun generate(tree: RandomTree): GenResultV2<Int> {
        val value = tree.random.nextInt(range)
        return buildResult(value)
    }

    override fun edgeCases(): List<GenResultV2<Int>> {
        return listOf(0, shrinkTarget, range.first, range.last)
            .flatMap { listOf(it, it + 1, it - 1) }
            .distinct()
            .filter { it in range }
            .map { buildResult(it) }
    }

    private fun buildResult(value: Int): GenResultV2<Int> = GenResultV2(
        value = value,
        shrinks = shrink(value, range, shrinkTarget).map { buildResult(it) }
    )
}
