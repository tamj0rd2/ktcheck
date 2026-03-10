package com.tamj0rd2.ktcheck.current

import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.contracts.GenResults
import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration
import com.tamj0rd2.ktcheck.Gen as IGen

internal abstract class BaseContractImpl : BaseContract, GenBuilders by GenV2Builders {
    //=== Wiring ===//
    override fun tree(seed: Seed) = RandomTree.new(seed)
    override fun Tree<*>.withLeft(left: Tree<*>) = (this as RandomTree).withLeft(left as RandomTree)
    override fun Tree<*>.withRight(right: Tree<*>) = (this as RandomTree).withRight(right as RandomTree)

    @Suppress("UNCHECKED_CAST")
    override fun <T> IGen<T>.generate(tree: Tree<*>): GenResults<T> {
        val result = (this as Gen).generate(tree as RandomTree, GenerationMode.InitialGeneration).orThrow()
        return GenResults(result.value, collectShrinksRecursively(result.shrinks))
    }

    override fun <T> IGen<T>.edgeCase(tree: Tree<*>): GenResults<T>? {
        val result = (this as Gen).edgeCase(tree as RandomTree, GenerationMode.InitialGeneration)
            .orThrow()
            ?: return null

        return GenResults(result.value, collectShrinksRecursively(result.shrinks))
    }

    override fun <T> IGen<T>.edgeCases(seed: Seed): List<GenResults<T>> =
        assertTimeoutPreemptively(Duration.ofSeconds(2)) {
            trees(seed)
                .take(1000)
                .mapNotNull { edgeCase(it) }
                .toList()
                .distinctBy { it.value }
        }

    private fun <T> Gen<T>.collectShrinksRecursively(shrinks: Sequence<RandomTree>): Sequence<GenResults<T>> =
        sequence {
            for (shrink in shrinks) {
                val result = generate(shrink, GenerationMode.Shrinking).onFailure { continue }
                yield(GenResults(result.value, collectShrinksRecursively(result.shrinks)))
            }
        }
}
