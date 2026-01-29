package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.shrinkers.BoolShrinker

internal class BoolGenV2(private val origin: Boolean) : GenV2<Boolean> {
    override fun generate(tree: ProducerTree): GenResultV2<Boolean> {
        val value = tree.random.nextBoolean()
        return GenResultV2(
            value = value,
            shrinks = BoolShrinker.shrink(value, origin).map { GenResultV2(it, emptySequence()) },
        )
    }
}
