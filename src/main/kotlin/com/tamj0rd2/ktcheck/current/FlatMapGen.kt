package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure

internal class FlatMapGen<T, R>(
    private val wrappedGen: Generator<T>,
    private val fn: (T) -> Generator<R>,
) : Generator<R> {
    override fun generate(root: RandomTree, mode: GenerationMode): Result4k<GeneratedValue<R>, GenerationException> {
        val outerResult = wrappedGen.generate(root.left, mode).onFailure { return it }
        val innerResult = fn(outerResult.value).generate(root.right, mode).onFailure { return it }
        return buildResult(root = root, outerResult = outerResult, innerResult = innerResult).asSuccess()
    }

    private fun buildResult(
        root: RandomTree,
        outerResult: GeneratedValue<T>,
        innerResult: GeneratedValue<R>,
    ): GeneratedValue<R> {
        val outerBasedShrinks = outerResult.shrinks.map { root.withLeft(it) }
        val innerBasedShrinks = innerResult.shrinks.map { root.withRight(it) }
        val shrinks = outerBasedShrinks + innerBasedShrinks

        return GeneratedValue(
            value = innerResult.value,
            shrinks = shrinks,
        )
    }
}
