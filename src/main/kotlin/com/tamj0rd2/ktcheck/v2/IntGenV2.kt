package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker.shrink

internal class IntGenV2(private val range: IntRange) : GenV2<Int> {
    override fun generate(tree: ProducerTree): GenResultV2<Int> {
        val value = tree.producer.int(range)

        return GenResultV2(
            value = value,
            shrinks = shrink(value, range).map {
                GenResultV2(it, emptySequence())
            }
        )
    }
}
