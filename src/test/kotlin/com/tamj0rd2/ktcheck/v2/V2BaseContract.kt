package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenFacade
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.contracts.GenResults
import com.tamj0rd2.ktcheck.core.ProducerTree

internal abstract class V2BaseContract : BaseContract, GenFacade by GenV2.Companion {
    override fun <T> Gen<T>.generate(tree: ProducerTree): GenResults<T> {
        val (value, shrinks) = (this as GenV2).generate(tree)
        return GenResults(value, collectShrinksRecursively2(shrinks))
    }

    private fun <T> GenV2<T>.collectShrinksRecursively(shrinks: Sequence<GenResultV2<T>>): Sequence<T> = sequence {
        for (shrink in shrinks) {
            yield(shrink.value)
            yieldAll(collectShrinksRecursively(shrink.shrinks))
        }
    }

    private fun <T> GenV2<T>.collectShrinksRecursively2(shrinks: Sequence<GenResultV2<T>>): Sequence<GenResults<T>> =
        sequence {
            for (shrink in shrinks) {
                yield(
                    GenResults(
                        value = shrink.value,
                        shrinks = collectShrinksRecursively2(shrink.shrinks)
                    )
                )
            }
        }
}
