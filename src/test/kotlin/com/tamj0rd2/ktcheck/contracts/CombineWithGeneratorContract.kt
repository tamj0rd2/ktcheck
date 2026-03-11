package com.tamj0rd2.ktcheck.contracts

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.first
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import strikt.assertions.isNotEmpty
import strikt.assertions.second

internal interface CombineWithGeneratorContract : BaseContract {
    override val exampleGen get() = int().combineWith(int(), ::Pair)

    @Test
    fun `combineWith merges two independent generators`() {
        val smallGen = int(0..5)
        val bigGen = int(10..20)
        val combinedGen = smallGen.combineWith(bigGen, ::Pair)

        repeatTest { seed ->
            val tree = tree(seed)
            val value = combinedGen.generate(tree).value
            expectThat(value).first.isIn(0..5)
            expectThat(value).second.isIn(10..20)
        }
    }

    @Test
    fun `combineWith combines shrinks from both generators`() {
        val oneToThree = int(1..3)
        val fourToSix = int(4..6)
        val gen = oneToThree.combineWith(fourToSix, ::Pair)

        val tree = tree()
            .withLeft(oneToThree.findTreeProducing(3))
            .withRight(fourToSix.findTreeProducing(6))

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo(3 to 6)
        expectThat(result).shrunkValues.isNotEmpty().contains(
            // first value shrunk
            1 to 6,
            // both values shrunk
            1 to 4,
            // second value shrunk
            3 to 4,
        )
    }

    @Test
    fun `when the left and right gen are equivalent, produces edge cases that cause both values to be equal`() {
        val gen1 = int(0..10)
        val gen2 = int(0..10)
        val combined = gen1.combineWith(gen2, ::Pair)

        repeatTest { seed ->
            val generatedValues = trees(seed).map { combined.generate(it) }.take(1000).toSet()
            expectThat(generatedValues.map { it.value }).contains(
                0 to 0,
                10 to 10,
            )
        }
    }
}
