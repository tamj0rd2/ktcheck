package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.RandomTree
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker.shrink
import kotlin.random.nextInt

internal class IntGenV2(
    private val range: IntRange,
    private val origin: Int = IntShrinker.defaultOrigin(range),
) : GenV2<Int> {
    override fun generate(tree: RandomTree): GenResultV2<Int> {
        val value = tree.random.nextInt(range)
        return buildResult(value)
    }

    override fun edgeCases(): List<GenResultV2<Int>> {
        return setOf(0, 1, -1, range.first, range.first + 1, range.last, range.last - 1)
            .filter { it in range }
            .map { buildResult(it) }
    }

    private fun buildResult(value: Int): GenResultV2<Int> = GenResultV2(
        value = value,
        shrinks = shrink(value, range, origin).map { buildResult(it) }
    )
}
