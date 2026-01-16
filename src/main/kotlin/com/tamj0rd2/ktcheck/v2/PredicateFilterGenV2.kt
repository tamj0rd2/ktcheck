package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.GenerationException
import com.tamj0rd2.ktcheck.core.ProducerTree

internal class PredicateFilterGenV2<T>(
    private val gen: GenV2<T>,
    private val threshold: Int,
    private val predicate: (T) -> Boolean,
) : GenV2<T> {
    override fun generate(tree: ProducerTree): GenResultV2<T> {
        return generateSequence(tree) { it.right }.take(threshold).map { gen.generate(it.left) }
            .firstOrNull { predicate(it.value) }?.filterShrinks(predicate)
            ?: throw GenerationException.FilterLimitReached(threshold)
    }

    private fun GenResultV2<T>.filterShrinks(predicate: (T) -> Boolean): GenResultV2<T> =
        GenResultV2(
            value = value,
            shrinks = shrinks
                .filter { predicate(it.value) }
                .map { it.filterShrinks(predicate) }
        )
}
