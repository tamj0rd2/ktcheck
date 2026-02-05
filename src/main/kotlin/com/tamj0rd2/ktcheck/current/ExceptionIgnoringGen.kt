package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException
import kotlin.reflect.KClass

internal class ExceptionIgnoringGen<T>(
    private val gen: GenImpl<T>,
    private val threshold: Int,
    private val klass: KClass<out Throwable>,
) : GenImpl<T>() {
    override fun generate(tree: RandomTree): GenResultV2<T> {
        var lastFailure: Throwable? = null

        return generateSequence(tree) { it.right }
            .take(threshold)
            .firstNotNullOfOrNull {
                catchException { gen.generate(it.left) }
                    .onFailure { ex -> lastFailure = ex }
                    .getOrNull()
            }
            ?.filterOutThrowingShrinks()
            ?: throw GenerationException.FilterLimitReached(threshold, lastFailure)
    }

    private fun GenResultV2<T>.filterOutThrowingShrinks(): GenResultV2<T> =
        copy(
            shrinks = sequence {
                val shrinksIter = shrinks.iterator()
                while (shrinksIter.hasNext()) {
                    val shrinkResult = catchException { shrinksIter.next() }.getOrNull()
                    if (shrinkResult == null) continue
                    yield(shrinkResult.filterOutThrowingShrinks())
                }
            }
        )

    private fun catchException(block: () -> GenResultV2<T>): Result<GenResultV2<T>> {
        return runCatching(block).onFailure { if (!klass.isInstance(it)) throw it }
    }
}
