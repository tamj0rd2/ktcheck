package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class FilterGen<T>(
    private val gen: GenImpl<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenImpl<T>() {
    // todo: rename tree to root tree.
    override fun generate(tree: RandomTree): GenResultV2<T> {
        return generateSequence(tree) { it.right }
            .take(threshold)
            .map { buildResult(tree, gen.generate(it.left)) }
            .firstOrNull { predicate(it.value) }
            ?: throw GenerationException.FilterLimitReached(threshold)
    }

    override fun edgeCases(tree: RandomTree): List<GenResultV2<T>> {
        return gen.edgeCases(tree)
            .map { buildResult(tree, it) }
            .filter { predicate(it.value) }
    }

    private fun buildResult(
        tree: RandomTree,
        result: GenResultV2<T>,
    ): GenResultV2<T> = GenResultV2(
        value = result.value,
        tree = tree.withLeft(result.tree),
        shrinks = result.shrinks.map { tree.withLeft(it) },
    )
}
