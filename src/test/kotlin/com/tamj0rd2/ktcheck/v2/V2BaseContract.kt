package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.core.ProducerTree

internal abstract class V2BaseContract : BaseContract, GenFacade by GenV2.Companion {
    override fun <T> Gen<T>.generate(tree: ProducerTree): T {
        return (this as GenV2).generate(tree).value
    }

    override fun <T> Gen<T>.generateWithShrunkValues(tree: ProducerTree): Pair<T, List<T>> {
        val (value, shrinks) = (this as GenV2).generate(tree)
        return value to shrinks.map { it.value }.toList()
    }

    override fun <T> Gen<T>.generateWithDeepShrinks(tree: ProducerTree): Pair<T, Sequence<T>> {
        val (value, shrinks) = (this as GenV2).generate(tree)
        return value to collectShrinksRecursively(shrinks)
    }

    private fun <T> GenV2<T>.collectShrinksRecursively(shrinks: Sequence<GenResultV2<T>>): Sequence<T> = sequence {
        for (shrink in shrinks) {
            yield(shrink.value)
            yieldAll(collectShrinksRecursively(shrink.shrinks))
        }
    }
}
