package com.tamj0rd2.ktcheck.v1

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.contracts.GenResults
import com.tamj0rd2.ktcheck.core.ProducerTree

internal abstract class V1BaseContract : BaseContract, GenFacade by GenV1.Companion {
    override fun <T> Gen<T>.generate(tree: ProducerTree): GenResults<T> {
        val (value, shrinks) = (this as GenV1).generate(tree, GenMode.Initial)
        return GenResults(
            value = value,
            shrinks = collectShrinksRecursively2(shrinks)
        )
    }

    private fun <T> GenV1<T>.collectShrinksRecursively(shrinks: Sequence<ProducerTree>): Sequence<T> = sequence {
        for (tree in shrinks) {
            val result = generate(tree, GenMode.Shrinking)
            yield(result.value)
            yieldAll(collectShrinksRecursively(result.shrinks))
        }
    }

    private fun <T> GenV1<T>.collectShrinksRecursively2(shrinks: Sequence<ProducerTree>): Sequence<GenResults<T>> =
        sequence {
            for (tree in shrinks) {
                val result = generate(tree, GenMode.Shrinking)
                yield(
                    GenResults(
                        value = result.value,
                        shrinks = collectShrinksRecursively2(result.shrinks)
                    )
                )
            }
        }
}
