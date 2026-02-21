package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.FilterGeneratorContract
import com.tamj0rd2.ktcheck.contracts.repeatTest
import com.tamj0rd2.ktcheck.contracts.shrunkValues
import com.tamj0rd2.ktcheck.contracts.value
import com.tamj0rd2.ktcheck.core.Tree
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import kotlin.random.Random

internal class FilterGeneratorTest : BaseContractImpl(), FilterGeneratorContract {
    @Test
    fun `generated values and their shrinks are reproducible`() {
        repeatTest { seed ->
            val gen = GenV2Builders.int(0..10).filter(100) { it > 5 }

            val originalResult = gen.generate(tree(seed))
            val originalShrunkValues = originalResult.shrinks.map { gen.generate(it).value }.distinct().toList()
            val regenerated = gen.generate(originalResult.tree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(originalResult.value)
            expectThat(regenerated).shrunkValues.containsExactlyInAnyOrder(originalShrunkValues)
            // this is the assertion I actually want, but the output is easier to read when split into 2 assertions.
            expectThat(regenerated).shrunkValues.isEqualTo(originalShrunkValues)
        }
    }

    // todo: at some point, this test needs to be included for all gens. it's a nice generic property
    //  that gives some very good reassurances. might also want to clarify the test name. all gens are deterministic
    //  due to the seed, but this particularly checks that you can rebuild exactly the same value using the returned tree.
    //  the returned tree essentially acts as a shortcut to quickly build the value - handy for edge case generation.
    @Test
    fun `edge cases and their shrinks are reproducible`() {
        repeatTest { seed ->
            val gen = GenV2Builders.int(0..10).filter(100) { it > 5 }
            val edgeCases = gen.edgeCases(tree(seed))

            val anEdgeCase = edgeCases.random(Random(seed.value))
            val originalShrunkValues = anEdgeCase.shrinks.map { gen.generate(it).value }.distinct().toList()
            val regenerated = gen.generate(anEdgeCase.tree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(anEdgeCase.value)
            expectThat(regenerated).shrunkValues.containsExactlyInAnyOrder(originalShrunkValues)
            // this is the assertion I actually want, but the output is easier to read when split into 2 assertions.
            expectThat(regenerated).shrunkValues.isEqualTo(originalShrunkValues)
        }
    }
}
