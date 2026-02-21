package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import org.junit.jupiter.api.Test
import strikt.api.expectDoesNotThrow
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.map

internal interface CombinatorGeneratorContract : BaseContract {
    @Test
    fun `constant always produces the same value and doesn't shrink`() {
        // todo: move this test elsewhere.
        val gen = constant(10)
        repeatTest {
            val result = gen.generate(tree())
            expectThat(result.value).isEqualTo(10)
            expectThat(result).shrunkValues.isEmpty()
            expectThat(gen.edgeCases()).isEmpty()
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

    // todo: add edge case tests for map.

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
            // first value shrunk
            1 to 6,
            // second value shrunk
            3 to 4,
        )
    }

    @Test
    fun `combineWith produces edge case permutations from both generators`() {
        val gen1 = int(0..10)
        val gen2 = int(0..10)
        val combined = gen1.combineWith(gen2, ::Pair)

        val edgeCases = combined.edgeCases()
        expectThat(edgeCases.map { it.value }).contains(
            0 to 0,
            0 to 10,
            10 to 0,
            10 to 10,
        )

        val shrinksFor10 = IntShrinker.shrink(10, 0..10, 0).toList()
        val edgeCaseWhereFirstIs10 = edgeCases.first { it.value.first == 10 }
        expectThat(edgeCaseWhereFirstIs10).shrunkValues.map { it.first }.isEqualTo(shrinksFor10)

        val edgeCaseWhereSecondIs10 = edgeCases.first { it.value.second == 10 }
        expectThat(edgeCaseWhereSecondIs10).shrunkValues.map { it.second }.isEqualTo(shrinksFor10)
    }

    @Test
    fun `combineWith can still produce edge cases for the first generator if the second generator has no edge cases`() {
        val gen1 = int(0..10)
        val gen2 = constant("hello")
        val combinedGen = gen1.combineWith(gen2, ::Pair)

        expectThat(gen1.edgeCases()).describedAs { "gen1 edge cases - $this" }.isNotEmpty()
        expectThat(gen2.edgeCases()).describedAs { "gen2 edge cases - $this" }.isEmpty()
        expectThat(combinedGen.edgeCases())
            .describedAs { "combined edge cases - $this" }
            .isNotEmpty()
            .map { it.value.first }
            .isEqualTo(gen1.edgeCases().map { it.value })
    }

    @Test
    fun `combineWith can still produce edge cases for the second generator if the first generator has no edge cases`() {
        val gen1 = constant("hello")
        val gen2 = int(0..10)
        val combinedGen = gen1.combineWith(gen2, ::Pair)

        expectThat(gen1.edgeCases()).describedAs { "gen1 edge cases - $this" }.isEmpty()
        expectThat(gen2.edgeCases()).describedAs { "gen2 edge cases - $this" }.isNotEmpty()
        expectThat(combinedGen.edgeCases())
            .describedAs { "combined edge cases - $this" }
            .isNotEmpty()
            .map { it.value.second }
            .isEqualTo(gen2.edgeCases().map { it.value })
    }

    @Test
    fun `combineWith doesn't yield any edge cases if neither generator has any`() {
        val gen1 = constant("hello")
        val gen2 = constant("world")
        val combinedGen = gen1.combineWith(gen2, ::Pair)

        expectThat(gen1.edgeCases()).describedAs { "gen1 edge cases - $this" }.isEmpty()
        expectThat(gen2.edgeCases()).describedAs { "gen2 edge cases - $this" }.isEmpty()
        expectThat(combinedGen.edgeCases()).isEmpty()
    }
}
