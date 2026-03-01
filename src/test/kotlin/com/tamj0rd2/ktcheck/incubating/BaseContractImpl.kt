package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.Gen
import com.tamj0rd2.ktcheck.GenBuilders
import com.tamj0rd2.ktcheck.contracts.BaseContract
import com.tamj0rd2.ktcheck.contracts.GenResults
import com.tamj0rd2.ktcheck.contracts.repeatTest
import com.tamj0rd2.ktcheck.contracts.shrunkValues
import com.tamj0rd2.ktcheck.contracts.skipIteration
import com.tamj0rd2.ktcheck.contracts.value
import com.tamj0rd2.ktcheck.core.Seed
import com.tamj0rd2.ktcheck.core.Tree
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.valueOrNull
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

internal abstract class BaseContractImpl : BaseContract, GenBuilders by GenV2Builders {
    @Test
    fun `generated values are reproducible via their returned tree`() {
        repeatTest { seed ->
            val gen = getGenIfDefined() as GenImpl
            val originalResult = gen.generate(tree(seed)).orThrow()
            val regenerated = gen.generate(originalResult.usedTree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(originalResult.value)
        }
    }

    @Test
    open fun `shrinks of generated values are reproducible via their returned tree`() {
        Assumptions.assumeTrue(genSupportsShrinking, "skipped as this gen doesn't support shrinking")

        repeatTest { seed ->
            val gen = getGenIfDefined() as GenImpl
            val originalResult = gen.generate(tree(seed)).orThrow()
            val originalShrunkValues = originalResult.shrinks
                .mapNotNull { gen.generate(it).valueOrNull()?.value }
                .distinct()
                .toList()
            if (originalShrunkValues.isEmpty()) skipIteration()

            val regenerated = gen.generate(originalResult.usedTree as Tree<*>)

            expectThat(regenerated).shrunkValues.isEqualTo(originalShrunkValues)
        }
    }

    @Test
    open fun `edge cases are reproducible via their returned tree`() {
        Assumptions.assumeTrue(genSupportsEdgeCases, "skipped as this gen doesn't support edge cases")

        repeatTest { seed ->
            val gen = getGenIfDefined() as GenImpl
            val edgeCases = gen.edgeCases(tree(seed))

            val anEdgeCase = edgeCases.random(Random(seed.value))
            val originalShrunkValues = anEdgeCase.shrinks.map { gen.generate(it).orThrow().value }.distinct().toList()
            val regenerated = gen.generate(anEdgeCase.usedTree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(anEdgeCase.value)
            expectThat(regenerated).shrunkValues.isEqualTo(originalShrunkValues)
        }
    }

    @Test
    open fun `shrinks of edge cases are reproducible via their returned tree`() {
        Assumptions.assumeTrue(genSupportsEdgeCases, "skipped as this gen doesn't support edge cases")
        Assumptions.assumeTrue(genSupportsShrinking, "skipped as this gen doesn't support shrinking")

        repeatTest { seed ->
            val gen = getGenIfDefined() as GenImpl
            val edgeCases = gen.edgeCases(tree(seed))

            val anEdgeCase = edgeCases.random(Random(seed.value))
            val originalShrunkValues = anEdgeCase.shrinks.map { gen.generate(it).orThrow().value }.distinct().toList()
            val regenerated = gen.generate(anEdgeCase.usedTree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(anEdgeCase.value)
            expectThat(regenerated).shrunkValues.isEqualTo(originalShrunkValues)
        }
    }

    //=== Wiring ===//
    override fun tree(seed: Seed) = RandomTree.new(seed)
    override fun Tree<*>.withLeft(left: Tree<*>) = (this as RandomTree).withLeft(left as RandomTree)
    override fun Tree<*>.withRight(right: Tree<*>) = (this as RandomTree).withRight(right as RandomTree)

    @Suppress("UNCHECKED_CAST")
    override fun <T> Gen<T>.generate(tree: Tree<*>): GenResults<T> {
        val result = (this as GenImpl).generate(tree as RandomTree).orThrow()
        return GenResults(result.value, collectShrinksRecursively(result.shrinks))
    }

    override fun <T> Gen<T>.edgeCases(): List<GenResults<T>> {
        val result = (this as GenImpl).edgeCases(RandomTree.forEdgeCases)
        return result.map { GenResults(it.value, collectShrinksRecursively(it.shrinks)) }
    }

    private fun <T> GenImpl<T>.collectShrinksRecursively(shrinks: Sequence<RandomTree>): Sequence<GenResults<T>> =
        sequence {
            for (shrink in shrinks) {
                val result = generate(shrink).onFailure { continue }
                yield(GenResults(result.value, collectShrinksRecursively(result.shrinks)))
            }
        }
}
