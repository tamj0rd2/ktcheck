package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.combineWith
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.flatMap
import com.tamj0rd2.ktcheck.v1.GenV1.Companion.map
import com.tamj0rd2.ktcheck.v1.PredicateResult.Failed
import com.tamj0rd2.ktcheck.v1.PredicateResult.Succeeded
import kotlin.random.Random
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
sealed class GenV1<T> {
    internal abstract fun GenContext.generate(): GenResult<T>

    internal fun generate(tree: ProducerTree, mode: GenMode): GenResult<T> =
        GenContext(tree, mode).generate()

    // todo: these ones belong on base class once it exists
    /**
     * Produces an infinite sequence of samples from the generator using the provided seed.
     *
     * @param random The random instance used to create seeds for sampling. Defaults to [Random.Default].
     * @return A sequence of sampled values of type T.
     */
    fun samples(seed: Long = Random.nextLong()) =
        generateSequence(Seed(seed)) { it.next(0) }.map { sample(it.value) }

    /**
     * Samples a value from the generator using the provided seed.
     *
     * @param seed The seed to use for sampling.
     * @return A sampled value of type T.
     */
    fun sample(seed: Long = Random.nextLong()): T = generate(
        tree = ProducerTree.new(Seed(seed)),
        mode = GenMode.Initial
    ).value

    companion object : GenBuilders by GenV1Builders
}

private object GenV1Builders : GenBuilders {
    override fun <T, R> GenV1<T>.map(fn: (T) -> R): GenV1<R> = CombinatorGenerator {
        val (value, shrinks) = generate(tree, mode)
        GenResult(fn(value), shrinks)
    }

    override fun <T, R> GenV1<T>.flatMap(fn: (T) -> GenV1<R>): GenV1<R> = CombinatorGenerator {
        val (leftValue, leftShrinks) = generate(tree.left, mode)
        val (rightValue, rightShrinks) = fn(leftValue).generate(tree.right, mode)
        GenResult(
            value = rightValue,
            shrinks = tree.combineShrinks(leftShrinks, rightShrinks)
        )
    }

    override fun <T1, T2, R> GenV1<T1>.combineWith(nextGen: GenV1<T2>, combine: (T1, T2) -> R): GenV1<R> =
        CombinatorGenerator {
            val (thisValue, thisShrinks) = generate(tree.left, mode)
            val (nextValue, nextShrinks) = nextGen.generate(tree.right, mode)
            GenResult(
                value = combine(thisValue, nextValue),
                shrinks = tree.combineShrinks(thisShrinks, nextShrinks)
            )
        }

    override fun <T> GenV1<T>.filter(threshold: Int, predicate: (T) -> Boolean): GenV1<T> =
        FilterGenerator(threshold) {
            val result = generate(tree, mode)
            if (predicate(result.value)) Succeeded(result) else Failed()
        }

    override fun <T> GenV1<T>.ignoreExceptions(klass: KClass<out Exception>, threshold: Int): GenV1<T> =
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

    override fun <T> constant(value: T): GenV1<T> = ConstantGenerator(value)

    override fun bool(): GenV1<Boolean> = BooleanGenerator()

    override fun int(range: IntRange): GenV1<Int> = IntGenerator(range)

    // todo: implement this properly
    override fun long() = int().map { it.toLong() }

    override fun <T> GenV1<T>.list(size: IntRange, distinct: Boolean): GenV1<List<T>> =
        ListGenerator(sizeRange = size, distinct = distinct, gen = this)

    override fun <T> oneOf(gens: Collection<GenV1<T>>): GenV1<T> = OneOfGenerator(gens.toList())

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
