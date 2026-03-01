package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class FilterGen<T>(
    private val gen: GenImpl<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenImpl<T>() {
    override fun generate(root: RandomTree): GeneratedValue<T> {
        return root.traversingRight()
            .take(threshold)
            .map { gen.generate(it.left) }
            .filter { predicate(it.value) }
            .map { buildResult(root, it) }
            .firstOrNull()
            ?: throw GenerationException.FilterLimitReached(threshold)
    }

    override fun edgeCases(root: RandomTree): List<GeneratedValue<T>> {
        return gen.edgeCases(root)
            .filter { predicate(it.value) }
            .map { buildResult(root, it) }
    }

    private fun buildResult(
        root: RandomTree,
        result: GeneratedValue<T>,
    ): GeneratedValue<T> {
        check(predicate(result.value)) { "internal error - value did not match the predicate" }

        return GeneratedValue(
            value = result.value,
            shrinks = result.shrinks
                .filter { predicate(gen.generate(it).value) }
                .map { root.withLeft(it) },
            usedTree = root.withLeft(result.usedTree),
        )
    }
}
