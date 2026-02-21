package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.ListGeneratorContract
import com.tamj0rd2.ktcheck.contracts.repeatTest
import com.tamj0rd2.ktcheck.contracts.shrunkValues
import com.tamj0rd2.ktcheck.contracts.value
import com.tamj0rd2.ktcheck.core.Tree
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import kotlin.random.Random

internal class ListGeneratorTest : BaseContractImpl(), ListGeneratorContract {
    @Test
    fun `generated values and their shrinks are reproducible`() {
        repeatTest { seed ->
            val gen = GenV2Builders.int(0..10).list(0..4)

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
    fun `edge cases and their shrinks are reproducible`() {
        repeatTest { seed ->
            val gen = GenV2Builders.int(0..10).list(0..5)
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
