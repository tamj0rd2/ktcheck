package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.contracts.GenResults
import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import com.tamj0rd2.ktcheck.Gen as IGen

internal abstract class BaseContractImpl : BaseContract, GenBuilders by GenV2Builders {
    //=== Wiring ===//
    override fun tree(seed: Seed) = RandomTree.new(seed)
    override fun Tree<*>.withLeft(left: Tree<*>) = (this as RandomTree).withLeft(left as RandomTree)
    override fun Tree<*>.withRight(right: Tree<*>) = (this as RandomTree).withRight(right as RandomTree)

    @Suppress("UNCHECKED_CAST")
    override fun <T> IGen<T>.generate(tree: Tree<*>): GenResults<T> {
        val result = (this as Gen).generate(tree as RandomTree).orThrow()
        return GenResults(result.value, collectShrinksRecursively(result.shrinks))
    }

    // TODO: re-implement this. I'm having second thoughts about edge cases.
    override fun <T> IGen<T>.edgeCases(tree: Tree<*>): List<GenResults<T>> {
        /**
         * I don't think I ever should have introduced an EdgeCases method. If you have a flatmap for example,
         * the only time you'll ever see edge cases are if the left and right gens both produce edge cases.
         * You don't get edge cases alongside other random generations. which is significantly less useful.
         *
         * An edge case needs to be something that's used alongside other randomly generated values
         */
        return emptyList()
    }

    private fun <T> Gen<T>.collectShrinksRecursively(shrinks: Sequence<RandomTree>): Sequence<GenResults<T>> =
        sequence {
            for (shrink in shrinks) {
                val result = generate(shrink).onFailure { continue }
                yield(GenResults(result.value, collectShrinksRecursively(result.shrinks)))
            }
        }
}
