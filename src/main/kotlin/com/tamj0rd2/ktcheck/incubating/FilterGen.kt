package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.GenerationException

internal class FilterGen<T>(
    private val gen: GenImpl<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenImpl<T>() {
    override fun generate(tree: RandomTree): GenResultV2<T> {
        return generateSequence(tree) { it.right }
            .take(threshold)
            .map { gen.generate(it.left) }
            .firstOrNull { predicate(it.value) }
            ?: throw GenerationException.FilterLimitReached(threshold)
    }

    override fun edgeCases(): List<GenResultV2<T>> {
        return gen.edgeCases().filter { predicate(it.value) }
    }
}
