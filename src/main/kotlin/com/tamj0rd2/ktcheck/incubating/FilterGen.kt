package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class FilterGen<T>(
    private val gen: GenImpl<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenImpl<T>() {
    override fun generate(root: RandomTree): GenResultV2<T> {
        return root.traversingRight()
            .take(threshold)
            .map { gen.generate(it.left) }
            .filter { predicate(it.value) }
            .map { buildResult(root, it) }
            .firstOrNull()
            ?: throw GenerationException.FilterLimitReached(threshold)
    }

    override fun edgeCases(root: RandomTree): List<GenResultV2<T>> {
        return gen.edgeCases(root)
            .filter { predicate(it.value) }
            .map { buildResult(root, it) }
    }

    private fun buildResult(
        root: RandomTree,
        result: GenResultV2<T>,
    ): GenResultV2<T> {
        check(predicate(result.value)) { "internal error - value did not match the predicate" }

        return GenResultV2(
            value = result.value,
            tree = root.withLeft(result.tree),
            shrinks = result.shrinks
                .filter { predicate(gen.generate(it).value) }
                .map { root.withLeft(it) },
        )
    }
}
