package com.tamj0rd2.ktcheck.genv2

data class GenResult<T>(val value: T, val shrinks: Sequence<SampleTree>)

fun interface Gen2<T> {
    fun generate(tree: SampleTree): GenResult<T>

    fun <R> map(fn: (T) -> R): Gen2<R> = Gen2 { tree ->
        val (value, shrinks) = generate(tree)
        GenResult(fn(value), shrinks)
    }

    fun <R> flatMap(fn: (T) -> Gen2<R>): Gen2<R> = Gen2 { tree ->
        val (leftValue, leftShrinks) = generate(tree.left)
        val (rightValue, rightShrinks) = fn(leftValue).generate(tree.right)
        GenResult(rightValue, combineShrinks(tree, leftShrinks, rightShrinks))
    }

    fun list(size: IntRange = 0..100): Gen2<List<T>> = Gen2.int(size).flatMap(::list)

    fun list(n: Int): Gen2<List<T>> = List(n) { this }
        .fold(Gen2.constant(emptyList())) { listGen, elementGen ->
            listGen.flatMap { listSoFar ->
                elementGen.map { value -> listSoFar + value }
            }
        }

    companion object
}

fun <T> Gen2.Companion.constant(value: T): Gen2<T> = sample().map { value }

internal fun Gen2.Companion.sample(): Gen2<Sample> = Gen2 { tree ->
    val shrinks = shrink(tree.sample.value).map { tree.withSample(Sample.Shrunk(it)) }
    GenResult(tree.sample, shrinks)
}

private fun shrink(value: ULong) = sequence {
    if (value == 0UL) return@sequence
    yield(0uL)

    var current = value
    while (current > 0UL) {
        val diff = value - current
        if (diff > 0uL) yield(diff)
        current /= 2UL
    }
}
