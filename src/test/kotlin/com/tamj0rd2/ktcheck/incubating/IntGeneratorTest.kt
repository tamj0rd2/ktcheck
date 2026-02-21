package com.tamj0rd2.ktcheck.incubating

import com.tamj0rd2.ktcheck.contracts.IntGeneratorContract
import com.tamj0rd2.ktcheck.contracts.repeatTest
import com.tamj0rd2.ktcheck.contracts.shrunkValues
import com.tamj0rd2.ktcheck.contracts.value
import com.tamj0rd2.ktcheck.core.Tree
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import kotlin.random.Random

internal class IntGeneratorTest : BaseContractImpl(), IntGeneratorContract {
    @Test
    fun `throws if the given value provider would cause the generated value to be out of range`() {
        val gen = int(0..10)
        val tree = tree().withProvider(PredeterminedValueProvider(100))

        expectThrows<IllegalStateException> { gen.generate(tree) }
    }

    @Test
    fun `edge cases and their shrinks are reproducible`() {
        repeatTest { seed ->
            val gen = GenV2Builders.int(0..10)
            val edgeCases = gen.edgeCases(tree(seed))

            val anEdgeCase = edgeCases.random(Random(seed.value))
            val regenerated = gen.generate(anEdgeCase.tree as Tree<*>)

            expectThat(regenerated).value.isEqualTo(anEdgeCase.value)
            expectThat(regenerated).shrunkValues.isEqualTo(anEdgeCase.shrinks.map { gen.generate(it).value }.toList())
        }
    }
}
