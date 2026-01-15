package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.v1.PredicateResult.Failed
import com.tamj0rd2.ktcheck.v1.PredicateResult.Succeeded
import kotlin.reflect.KClass

private sealed interface PredicateResult<T> {
    @JvmInline
    value class Succeeded<T>(val genResult: GenResult<T>) : PredicateResult<T> {
        operator fun component1() = genResult
    }

    @JvmInline
    value class Failed<T>(val failure: Exception? = null) : PredicateResult<T>
}

private class FilterGenerator<T>(
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

class FilterLimitReached internal constructor(threshold: Int, cause: Throwable?) :
    GenerationException("Filter failed after $threshold misses", cause)

/**
 * Filters generated values using the given [predicate]. Although this generator supports shrinking, it is very
 * inefficient. Instead of using this generator, consider using generators that do not throw exceptions.
 */
fun <T> GenV1<T>.filter(predicate: (T) -> Boolean) = filter(100, predicate)

/**
 * Filters generated values using the given [predicate]. Although this generator supports shrinking, it is very
 * inefficient. Instead of using this generator, consider using generators that do not throw exceptions.
 */
fun <T> GenV1<T>.filter(threshold: Int, predicate: (T) -> Boolean): GenV1<T> =
    FilterGenerator(threshold) {
        val result = generate(tree, mode)
        if (predicate(result.value)) Succeeded(result) else Failed()
    }

/**
 * Ignores exceptions of type [klass] thrown during generation. Although this generator supports shrinking, it is very
 * inefficient. Instead of using this generator, consider using generators that do not throw exceptions.
 */
fun <T> GenV1<T>.ignoreExceptions(klass: KClass<out Exception>, threshold: Int = 100): GenV1<T> =
    FilterGenerator(threshold) {
        try {
            Succeeded(generate(tree, mode))
        } catch (e: Exception) {
            when {
                !klass.isInstance(e) -> throw e
                else -> Failed(e)
            }
        }
    }
