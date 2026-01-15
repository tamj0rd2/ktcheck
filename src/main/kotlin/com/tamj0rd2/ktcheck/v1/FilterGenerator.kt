package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.GenerationException.FilterLimitReached
import com.tamj0rd2.ktcheck.v1.PredicateResult.Failed
import com.tamj0rd2.ktcheck.v1.PredicateResult.Succeeded

internal sealed interface PredicateResult<T> {
    @JvmInline
    value class Succeeded<T>(val genResult: GenResult<T>) : PredicateResult<T> {
        operator fun component1() = genResult
    }

    @JvmInline
    value class Failed<T>(val failure: Exception? = null) : PredicateResult<T>
}

internal class FilterGenerator<T>(
    private val threshold: Int,
    private val getResult: GenContext.() -> PredicateResult<T>,
) : GenV1<T>() {
    override fun GenContext.generate(): GenResult<T> {
        var lastFailure: Exception? = null

        return generateSequence(tree) { it.right }
            .take(threshold)
            .map { getResult(GenContext(it.left, mode)) }
            .onEach { if (it is Failed) lastFailure = it.failure }
            .filterIsInstance<Succeeded<T>>()
            .map { (genResult) ->
                val validShrinks = genResult.shrinks
                    .filter { getResult(GenContext(it, GenMode.Shrinking)) is Succeeded }
                    .map { tree.withLeft(it) }

                genResult.copy(shrinks = validShrinks)
            }
            .firstOrNull()
            ?: throw FilterLimitReached(threshold, lastFailure)
    }
}

