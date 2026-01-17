package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.GenerationException
import com.tamj0rd2.ktcheck.core.ProducerTree

internal class PredicateFilterGenV2<T>(
    private val gen: GenV2<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenV2<T> {
    override fun generate(tree: ProducerTree): GenResultV2<T> {
        return generateSequence(tree) { it.right }
            .take(threshold)
            .map { gen.generate(it.left) }
            .firstNotNullOfOrNull { it.filter(predicate) }
            ?: throw GenerationException.FilterLimitReached(threshold)
    }
}
