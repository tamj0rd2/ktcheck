package com.tamj0rd2.ktcheck.contracts

import org.junit.jupiter.api.Test
import strikt.api.expectDoesNotThrow
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

internal interface FlatMapGeneratorContract : BaseContract {
    override val exampleGen get() = int(0..5).flatMap { int(10..10 + it) }

    @Test
    fun `flatMap generates the second value based on the first`() {
        val smallGen = int(0..5)
        val bigGen = int(10..20)
        val gen = smallGen.flatMap { a -> bigGen.map { b -> a + b } }

        repeatTest { seed ->
            val tree = tree(seed)
            val expectedOuterValue = smallGen.generate(tree.left).value
            val expectedInnerValue = bigGen.generate(tree.right).value

            val value = gen.generate(tree).value
            expectThat(value).isEqualTo(expectedOuterValue + expectedInnerValue)
        }
    }

    @Test
    fun `flatMap combines shrinks from both generators`() {
        val oneToThree = int(1..3)
        val fourToSix = int(4..6)
        val gen = oneToThree.flatMap { outer ->
            fourToSix.map { inner ->
                Pair(outer, inner)
            }
        }

        val tree = tree()
            .withLeft(oneToThree.findTreeProducing(3))
            .withRight(fourToSix.findTreeProducing(6))

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo(3 to 6)
        expectThat(result).shrunkValues.contains(
            // outer value shrunk
            1 to 6,
            // inner value shrunk
            3 to 4,
        )
    }

    @Test
    fun `flatMap allows changing the constraints of the inner generator`() {
        val gen = int(0..2).flatMap { int(10..10 + it) }

        // would require that the outer generator produced a 2
        val result = gen.generating(12)
        expectThat(result.value).isEqualTo(12)
        expectDoesNotThrow { result.shrunkValues.toSet() }
    }

    // todo: add tests for edge cases in flatMap.
}
