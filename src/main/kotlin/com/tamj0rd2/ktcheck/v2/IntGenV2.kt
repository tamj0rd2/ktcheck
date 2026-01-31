package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.RandomTree
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker.shrink
import kotlin.random.nextInt

internal class IntGenV2(private val range: IntRange) : GenV2<Int> {
    override fun generate(tree: RandomTree): GenResultV2<Int> {
        val value = tree.random.nextInt(range)
        return buildResult(value)
    }

    private fun buildResult(value: Int): GenResultV2<Int> = GenResultV2(
        value = value,
        shrinks = shrink(value, range).map { buildResult(it) }
    )
}
