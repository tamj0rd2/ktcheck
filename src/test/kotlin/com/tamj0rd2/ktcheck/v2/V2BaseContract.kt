package com.tamj0rd2.ktcheck.v2

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.contracts.GenResults
import com.tamj0rd2.ktcheck.core.Seed

internal abstract class V2BaseContract : BaseContract, GenBuilders by GenV2Builders {
    override fun tree(seed: Seed) = randomTree(seed)

    @Suppress("UNCHECKED_CAST")
    override fun <T> Gen<T>.generate(tree: RandomTree): GenResults<T> {
        val result = (this as GenV2).generate(tree)
        return GenResults(result.value, collectShrinksRecursively(result.shrinks))
    }

    private fun <T> GenV2<T>.collectShrinksRecursively(shrinks: Sequence<GenResultV2<T>>): Sequence<GenResults<T>> =
        sequence {
            for (shrink in shrinks) {
                yield(
                    GenResults(
                        value = shrink.value,
                        shrinks = collectShrinksRecursively(shrink.shrinks)
                    )
                )
            }
        }

}
