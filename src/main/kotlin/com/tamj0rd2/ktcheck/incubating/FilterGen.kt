package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class FilterGen<T>(
    private val gen: GenImpl<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenImpl<T>() {
    override fun generate(root: RandomTree): GenResultV2<T> {
        return generateSequence(root) { it.right }
            .take(threshold)
            .map { buildResult(root, gen.generate(it.left)) }
            .firstOrNull { predicate(it.value) }
            ?: throw GenerationException.FilterLimitReached(threshold)
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<T>> {
        return gen.edgeCases(root)
            .map { buildResult(root, it) }
            .filter { predicate(it.value) }
    }

    private fun buildResult(
        root: RandomTree,
        result: GenResultV2<T>,
    ): GenResultV2<T> = GenResultV2(
        value = result.value,
        tree = root.withLeft(result.tree),
        shrinks = result.shrinks.map { root.withLeft(it) },
    )
}
