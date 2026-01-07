package com.tamj0rd2.ktcheck.gen

import com.tamj0rd2.ktcheck.producer.ProducerTree

private class CombinerGenerator<T>(
    private val block: CombinerContext.() -> T,
) : Gen<T>() {
    override fun GenContext.generate(): GenResult<T> {
        val initialTree = tree
        return CombinerContext(initialTree, mode).run {
            val value = block(this)
            val shrinks = initialTree.combineShrinks(shrinksByIndex)
            GenResult(value, shrinks)
        }
    }
}

class CombinerContext internal constructor(
    private var tree: ProducerTree,
    private val mode: GenMode,
) {
    internal val shrinksByIndex = mutableListOf<Sequence<ProducerTree>>()

    fun <T> Gen<T>.bind(): T {
        val (value, shrinks) = generate(tree.left, mode)
        tree = tree.right
        shrinksByIndex.add(shrinks)
        return value
    }
}

/**
 * Combines multiple generators into a single generator using a builder-style DSL.
 * Each generator in the block is bound sequentially, and their shrinks are combined.
 *
 * Example:
 * ```
 * val gen = Gen.combine {
 *     val x = Gen.int().bind()
 *     val y = Gen.int().bind()
 *     x to y
 * }
 * ```
 *
 * This is equivalent to using [plus] but with a more convenient syntax:
 * ```
 * val gen = (Gen.int() + Gen.bool()).map { (x, y) -> x + y }
 * ```
 *
 * **Warning about conditionals:** The combiner requires that bind functions will be called in the same order each time.
 * Conditionals that affect whether trailing [CombinerContext.bind] calls are called will shrink correctly.
 * However, conditionals that skip non-trailing [CombinerContext.bind] calls will cause invalid shrinks.
 */
fun <T> Gen.Companion.combine(block: CombinerContext.() -> T): Gen<T> = CombinerGenerator(block)


/**
 * Combines two independent generators into a single generator that produces a tuple of both values.
 * Shrinking is performed independently on each component.
 *
 * Example:
 * ```
 * // Gen<Pair<Int, Boolean>>
 * val gen2 = Gen.int() + Gen.boolean()
 * // Gen<Triple<Int, Boolean, String>>
 * val gen3 = Gen.int() + Gen.boolean() + Gen.string()
 * ```
 *
 * To combine more than 3 generators, use [Gen.Companion.combine] instead.
 *
 * For dependent generation (where the second generator depends on the first value),
 * use [flatMap] or [Gen.Companion.combine] instead.
 */
@JvmName("zip2")
infix operator fun <T1, T2> Gen<T1>.plus(
    nextGen: Gen<T2>,
) = combineWith(nextGen, ::Pair)

@JvmName("zip3")
infix operator fun <T1, T2, T3> Gen<Pair<T1, T2>>.plus(
    nextGen: Gen<T3>,
) = combineWith(nextGen) { pair, nextValue -> Triple(pair.first, pair.second, nextValue) }
