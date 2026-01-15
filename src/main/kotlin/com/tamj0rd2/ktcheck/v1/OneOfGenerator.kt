package com.tamj0rd2.ktcheck.v1

/**
 * A generator that chooses between multiple generators using an index. Shrinks towards
 * earlier specified generators.
 *
 * This implementation prevents type mismatches when generators produce different types
 * (cast to a common supertype) by using fresh trees when switching generators:
 * - When shrinking the value within the same generator, we keep the predetermined values
 * - When shrinking the index to switch generators, we use a fresh undetermined tree
 *   to avoid passing predetermined values from one generator type to another
 *
 * Consequently, when switching generators, we lose shrink progress on the value.
 * The new generator starts with a fresh value, not a continuation of shrinks.
 * This is the trade-off: safety (no type mismatches) vs optimal shrinking.
 **/
private class OneOfGenerator<T>(
    private val gens: List<GenV1<T>>,
) : GenV1<T>() {
    init {
        if (gens.isEmpty()) throw OneOfEmpty()
    }

    override fun GenContext.generate(): GenResult<T> {
        val (index, indexShrinks) = GenV1.int(0..<gens.size).generate(tree.left, mode)
        val (value, valueShrinks) = gens[index].generate(tree.right, mode)

        val shrinks = sequence {
            // allows switching between generators by shrinking the index with a fresh (undetermined) right tree
            yieldAll(indexShrinks.map { tree.withLeft(it).withRight(tree.right.right) })

            yieldAll(valueShrinks.map { tree.withRight(it) })
        }

        return GenResult(value = value, shrinks = shrinks)
    }
}

/** Shrinks towards the first generator */
fun <T> GenV1.Companion.oneOf(vararg gens: GenV1<T>): GenV1<T> = oneOf(gens.toList())

/** Shrinks toward the first generator */
fun <T> GenV1.Companion.oneOf(gens: Collection<GenV1<T>>): GenV1<T> = OneOfGenerator(gens.toList())

/** Shrinks toward the first value. Individual values will not be shrunk. */
@JvmName("oneOfValues")
fun <T> GenV1.Companion.oneOf(values: Iterable<T>): GenV1<T> {
    val options = values.toList()
    if (options.isEmpty()) throw OneOfEmpty()
    return GenV1.int(0..<options.size).map { options[it] }
}

@Suppress("unused")
class OneOfEmpty internal constructor() : GenerationException("Gen.oneOf() called with no generators")
