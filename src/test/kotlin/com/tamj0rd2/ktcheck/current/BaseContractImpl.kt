package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.contracts.GenResults
import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree

internal abstract class BaseContractImpl : BaseContract, GenBuilders by GenV2Builders {
    //=== Wiring ===//
    override fun tree(seed: Seed) = randomTree(seed)
    override fun Tree<*>.withLeft(left: Tree<*>) = (this as RandomTree).withLeft(left as RandomTree)
    override fun Tree<*>.withRight(right: Tree<*>) = (this as RandomTree).withRight(right as RandomTree)

    @Suppress("UNCHECKED_CAST")
    override fun <T> Gen<T>.generate(tree: Tree<*>): GenResults<T> {
        val result = (this as GenImpl).generate(tree as RandomTree)
        return GenResults(result.value, collectShrinksRecursively(result.shrinks))
    }

    private fun <T> Gen<T>.collectShrinksRecursively(shrinks: Sequence<GenResultV2<T>>): Sequence<GenResults<T>> =
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

    override fun <T> Gen<T>.edgeCases(): List<GenResults<T>> {
        val result = (this as GenImpl).edgeCases()
        return result.map { GenResults(it.value, collectShrinksRecursively(it.shrinks)) }
    }
}
