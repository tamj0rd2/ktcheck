package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.core.ProducerTree

internal abstract class V1BaseContract : BaseContract, GenFacade by GenV1.Companion {
    override fun <T> Gen<T>.generate(tree: ProducerTree): T {
        return (this as GenV1).generate(tree, GenMode.Initial).value
    }

    override fun <T> Gen<T>.generateWithShrunkValues(tree: ProducerTree): Pair<T, List<T>> {
        val (value, shrinks) = (this as GenV1).generate(tree, GenMode.Initial)
        return value to shrinks.map { generate(it, GenMode.Shrinking).value }.toList()
    }

    override fun <T> Gen<T>.generateWithDeepShrinks(tree: ProducerTree): Pair<T, Sequence<T>> {
        val (value, shrinks) = (this as GenV1).generate(tree, GenMode.Initial)
        return value to collectShrinksRecursively(shrinks)
    }

    private fun <T> GenV1<T>.collectShrinksRecursively(shrinks: Sequence<ProducerTree>): Sequence<T> = sequence {
        for (tree in shrinks) {
            val result = generate(tree, GenMode.Shrinking)
            yield(result.value)
            yieldAll(collectShrinksRecursively(result.shrinks))
        }
    }
}
