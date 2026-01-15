package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.combineWith
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.flatMap
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.map
import com.tamj0rd2.ktcheck.v1.PredicateResult.Failed
import com.tamj0rd2.ktcheck.v1.PredicateResult.Succeeded
import kotlin.reflect.KClass

internal data class GenContext(
    val tree: ProducerTree,
    val mode: GenMode,
)

internal enum class GenMode {
    Initial,
    Shrinking,
}

/**
 * A generator that can produce values of type T.
 *
 * Generators can be transformed and combined using various methods such as [map], [flatMap], and [combineWith].
 *
 * @param T The type of values produced by this generator.
 */
internal sealed class GenV1<T> : Gen<T> {
    internal abstract fun GenContext.generate(): GenResult<T>

    internal fun generate(tree: ProducerTree, mode: GenMode): GenResult<T> =
        GenContext(tree, mode).generate()

    companion object : GenFacade by GenV1Facade
}

private object GenV1Facade : GenFacade {
    override fun <T> Gen<T>.sample(seed: Long): T {
        return (this as GenV1).generate(tree = ProducerTree.new(Seed(seed)), mode = GenMode.Initial).value
    }

    override fun <T, R> Gen<T>.map(fn: (T) -> R): Gen<R> = CombinatorGenerator {
        val (value, shrinks) = (this@map as GenV1).generate(tree, mode)
        GenResult(fn(value), shrinks)
    }

    override fun <T, R> Gen<T>.flatMap(fn: (T) -> Gen<R>): Gen<R> = CombinatorGenerator {
        val (leftValue, leftShrinks) = (this@flatMap as GenV1).generate(tree.left, mode)
        val (rightValue, rightShrinks) = (fn(leftValue) as GenV1).generate(tree.right, mode)
        GenResult(
            value = rightValue,
            shrinks = tree.combineShrinks(leftShrinks, rightShrinks)
        )
    }

    override fun <T1, T2, R> Gen<T1>.combineWith(nextGen: Gen<T2>, combine: (T1, T2) -> R): Gen<R> =
        CombinatorGenerator {
            val (thisValue, thisShrinks) = (this@combineWith as GenV1).generate(tree.left, mode)
            val (nextValue, nextShrinks) = (nextGen as GenV1).generate(tree.right, mode)
            GenResult(
                value = combine(thisValue, nextValue),
                shrinks = tree.combineShrinks(thisShrinks, nextShrinks)
            )
        }

    override fun <T> Gen<T>.filter(threshold: Int, predicate: (T) -> Boolean): Gen<T> =
        FilterGenerator(threshold) {
            val result = (this@filter as GenV1).generate(tree, mode)
            if (predicate(result.value)) Succeeded(result) else Failed()
        }

    override fun <T> Gen<T>.ignoreExceptions(klass: KClass<out Exception>, threshold: Int): Gen<T> =
        FilterGenerator(threshold) {
            try {
                Succeeded((this@ignoreExceptions as GenV1).generate(tree, mode))
            } catch (e: Exception) {
                when {
                    !klass.isInstance(e) -> throw e
                    else -> Failed(e)
                }
            }
        }

    override fun <T> constant(value: T): GenV1<T> = ConstantGenerator(value)

    override fun bool(): GenV1<Boolean> = BooleanGenerator()

    override fun int(range: IntRange): GenV1<Int> = IntGenerator(range)

    // todo: implement this properly
    override fun long() = int().map { it.toLong() }

    override fun <T> Gen<T>.list(size: IntRange, distinct: Boolean): Gen<List<T>> =
        ListGenerator(sizeRange = size, distinct = distinct, gen = this as GenV1)

    override fun <T> oneOf(gens: Collection<Gen<T>>): Gen<T> = OneOfGenerator(gens.toList().map { it as GenV1 })

    override fun <T> combine(block: CombinerContext.() -> T): GenV1<T> = CombinerGenerator(block)
}

/**
 * The result of generating a value from a generator, including the generated value and its shrinks.
 * Shrinks are represented as a sequence of [ProducerTree]s, allowing for lazy evaluation and efficient traversal.
 */
internal data class GenResult<T>(val value: T, val shrinks: Sequence<ProducerTree>)

private class CombinatorGenerator<T>(private val generator: GenContext.() -> GenResult<T>) : GenV1<T>() {
    override fun GenContext.generate(): GenResult<T> = generator()
}

sealed class GenerationException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause)
