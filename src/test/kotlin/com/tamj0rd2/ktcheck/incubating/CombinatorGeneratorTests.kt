package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.CombinatorGeneratorContract
import com.tamj0rd2.ktcheck.contracts.repeatTest
import com.tamj0rd2.ktcheck.contracts.shrunkValues
import com.tamj0rd2.ktcheck.contracts.value
import com.tamj0rd2.ktcheck.core.Tree
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import kotlin.random.Random

internal class CombinatorGeneratorTests : BaseContractImpl(), CombinatorGeneratorContract {

    // todo: just move all of these into a concrete test factory for the incubator. then I can check all gens in one
    //  place.
    @Test
    fun `combineWith - generated values and their shrinks are reproducible`() {
        repeatTest { seed ->
            val gen = zip(int(1..3), int(4..6)) as GenImpl

            val originalResult = gen.generate(tree(seed))
            val originalShrunkValues = originalResult.shrinks.map { gen.generate(it).value }.distinct().toList()
            val regenerated = gen.generate(originalResult.tree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(originalResult.value)
            expectThat(regenerated).shrunkValues.containsExactlyInAnyOrder(originalShrunkValues)
            // this is the assertion I actually want, but the output is easier to read when split into 2 assertions.
            expectThat(regenerated).shrunkValues.isEqualTo(originalShrunkValues)
        }
    }

    @Test
    fun `combineWith - edge cases and their shrinks are reproducible`() {
        repeatTest { seed ->
            val gen = zip(int(1..3), int(4..6)) as GenImpl
            val edgeCases = gen.edgeCases(tree(seed))

            val anEdgeCase = edgeCases.random(Random(seed.value))
            val regenerated = gen.generate(anEdgeCase.tree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(anEdgeCase.value)
            expectThat(regenerated).shrunkValues.isEqualTo(anEdgeCase.shrinks.map { gen.generate(it).value }.toList())
        }
    }
}
