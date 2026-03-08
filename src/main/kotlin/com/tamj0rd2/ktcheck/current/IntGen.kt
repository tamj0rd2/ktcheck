package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess

internal class IntGen(
    private val range: IntRange,
    private val shrinkTarget: Int,
) : GenImpl<Int> {
    init {
        require(shrinkTarget in range) { "Shrink target $shrinkTarget must be within the range $range" }
    }

    override fun generate(root: RandomTree): Result4k<GeneratedValue<Int>, GenerationException> {
        val value = root.provider.int(range)
        return buildResult(root, value).asSuccess()
    }

    private fun buildResult(
        root: RandomTree,
        value: Int,
    ): GeneratedValue<Int> {
        val shrinks = IntShrinker.shrink(value, range, shrinkTarget).map { root.withPredeterminedValue(it) }

        return GeneratedValue(
            value = value,
            shrinks = shrinks,
        )
    }
}
