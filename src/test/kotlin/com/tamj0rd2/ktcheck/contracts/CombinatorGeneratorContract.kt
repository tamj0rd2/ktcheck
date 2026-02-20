package com.tamj0rd2.ktcheck.contracts

import org.junit.jupiter.api.Test
import strikt.api.expectDoesNotThrow
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

internal interface CombinatorGeneratorContract : BaseContract {
    @Test
    fun `constant always produces the same value and doesn't shrink`() {
        // todo: move this test elsewhere.
        val gen = constant(10)
        repeatTest {
            val result = gen.generate(tree())
            expectThat(result.value).isEqualTo(10)
            expectThat(result).shrunkValues.isEmpty()
        }
    }

    @Test
    fun `same seed produces same sample`() {
        // todo: move this test elsewhere. also, it's duplicated in CharGeneratorContract.
        repeatTest { seed ->
            val gen = int(-1000..1000)
            val firstRun = gen.generate(tree(seed))
            val secondRun = gen.generate(tree(seed))
            expectThat(firstRun.value).isEqualTo(secondRun.value)
        }
    }

    @Test
    fun `map maps the original value and shrinks`() {
        val originalGen = int(0..10)
        val doublingGen = originalGen.map { it * 2 }

        repeatTest { seed ->
            val originalResult = originalGen.generate(tree(seed))
            val doubledResult = doublingGen.generate(tree(seed))

            expectThat(doubledResult.value).isEqualTo(originalResult.value * 2)
            expectThat(doubledResult).shrunkValues.isEqualTo(originalResult.shrunkValues.map { it * 2 })
        }
    }

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
            // inner value shrunk
            3 to 4,
            // outer value shrunk
            1 to 6,
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

    @Test
    fun `combineWith merges two independent generators`() {
        val smallGen = int(0..5)
        val bigGen = int(10..20)
        val gen = smallGen.combineWith(bigGen) { a, b -> a + b }

        repeatTest { seed ->
            val tree = tree(seed)
            val expectedOuterValue = smallGen.generate(tree.left).value
            val expectedInnerValue = bigGen.generate(tree.right).value

            val value = gen.generate(tree).value
            expectThat(value).isEqualTo(expectedOuterValue + expectedInnerValue)
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
            // inner value shrunk
            3 to 4,
            // outer value shrunk
            1 to 6,
        )
    }

    @Test
    fun `combineWith produces edge case permutations from both generators`() {
        val gen1 = int(0..100)
        val gen2 = int(0..100)
        val combined = gen1.combineWith(gen2, ::Pair)

        val edgeCases = combined.edgeCases().map { it.value }.toSet()

        expectThat(edgeCases).contains(
            0 to 0,
            0 to 100,
            100 to 0,
            100 to 100,
        )
    }
}
