package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
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
        val value = root.provider.int(range)
        return buildResult(root, value).asSuccess()
    }

    private val edgeCases by lazy {
        listOf(0, range.first, range.last)
            .flatMap { listOf(it, it - 1, it + 1) }
            .distinct()
            .filter { it in range }
    }

    override fun edgeCase(root: RandomTree, mode: GenerationMode): Result4k<GeneratedValue<Int>?, GenerationException> {
        val edgeCase = edgeCases[root.provider.int(edgeCases.indices)]
        return buildResult(root = root, value = edgeCase).asSuccess()
    }

    private fun buildResult(
        root: RandomTree,
        value: Int,
    ): GeneratedValue<Int> {
        val shrinks = IntShrinker.shrink(value, range, shrinkTarget).map {
            root.withProvider(PredeterminedValueProvider(it, root.provider))
        }

        return GeneratedValue(value = value, shrinks = shrinks)
    }
}
