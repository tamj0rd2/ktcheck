package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenerationException

internal class PredicateFilterGen<T>(
    private val gen: GenImpl<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenImpl<T>() {
    override fun edgeCases(): List<GenResultV2<T>> {
        return gen.edgeCases().mapNotNull { it.filter(predicate) }
    }

    override fun generate(tree: RandomTree): GenResultV2<T> {
        return generateSequence(tree) { it.right }
            .take(threshold)
            .map { gen.generate(it.left) }
            .firstNotNullOfOrNull { it.filter(predicate) }
            ?: throw GenerationException.FilterLimitReached(threshold)
    }
}
