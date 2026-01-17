package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker

internal data class IntGenerator(
    private val range: IntRange,
) : GenV1<Int>() {
    override fun GenContext.generate(): GenResult<Int> {
        if (range.first == 10) {
            println("Generating an Int in range $range - ${tree.producer}")
        }

        val value = tree.producer.int(range)
        return GenResult(
            value = value,
            shrinks = IntShrinker.shrink(value, range).map { tree.withValue(it) }
        )
    }
}
