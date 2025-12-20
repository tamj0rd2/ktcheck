package com.tamj0rd2.ktcheck.genv2

private class ListGenerator<T>(
    private val size: Int,
    private val gen: Gen<T>,
) : Gen<List<T>>() {
    override fun generate(tree: SampleTree): GenResult<List<T>> = when (size) {
        1 -> list1().generate(tree)
        2 -> list2().generate(tree)
        else -> listN(tree)
    }

    private fun list1(): Gen<List<T>> = gen.map(::listOf)

    private fun list2(): Gen<List<T>> = (gen + gen).map { (a, b) -> listOf(a, b) }

    private tailrec fun listN(
        currentTree: SampleTree,
        remaining: Int = size,
        acc: List<T> = emptyList(),
        shrinks: Sequence<SampleTree> = emptySequence(),
    ): GenResult<List<T>> {
        if (remaining == 0) return GenResult(acc, shrinks)

        val (value, valueShrinks) = gen.generate(currentTree.left)

        return listN(
            currentTree = currentTree.right,
            remaining = remaining - 1,
            acc = acc + value,
            shrinks = combineShrinks(currentTree, shrinks, valueShrinks)
        )
    }
}

fun <T> Gen<T>.list(size: IntRange = 0..100): Gen<List<T>> = list(Gen.int(size))

fun <T> Gen<T>.list(size: Gen<Int>): Gen<List<T>> = size.flatMap(::list)

fun <T> Gen<T>.list(size: Int): Gen<List<T>> = ListGenerator(size, this)
