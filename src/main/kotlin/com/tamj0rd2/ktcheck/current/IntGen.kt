package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import com.tamj0rd2.ktcheck.current.GenerationMode.*
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess

internal class IntGen(
    private val range: IntRange,
    private val shrinkTarget: Int,
) : Generator<Int> {
    init {
        require(shrinkTarget in range) { "Shrink target $shrinkTarget must be within the range $range" }
    }

    override fun generate(root: RandomTree): Result4k<GeneratedValue<Int>, GenerationException> {
        val value = when (root.generationMode) {
            Random,
            Shrinking,
                -> root.provider.int(range)

            EdgeCase -> edgeCases[root.provider.int(edgeCases.indices)]
        }

        return GeneratedValue(
            value = value,
            shrinks = IntShrinker.shrink(value = value, range = range, target = shrinkTarget).map(root::withShrunkValue)
        ).asSuccess()
    }

    private val edgeCases by lazy {
        listOf(0, range.first, range.last)
            .flatMap { listOf(it, it - 1, it + 1) }
            .distinct()
            .filter { it in range }
    }
}
