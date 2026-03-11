package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import strikt.assertions.values
import kotlin.random.Random

internal interface IntGeneratorContract : BaseContract {
    override val exampleGen get() = int()

    @TestFactory
    fun `can generate an integer within a range`(): List<DynamicTest> {
        val testCases = mapOf(
            "0 to 0" to 0..0,
            "1 to 1" to 1..1,
            "-1 to -1" to -1..-1,
            "max to max" to Int.MAX_VALUE..Int.MAX_VALUE,
            "min to min" to Int.MIN_VALUE..Int.MIN_VALUE,
            "positive range" to 10..20,
            "negative range" to -20..-10,
            "mixed range" to -10..10,
            "full int range" to Int.MIN_VALUE..Int.MAX_VALUE,
        )

        return testCases.map { (desc, range) ->
            dynamicTest(desc) {
                int(range)
                    .samples()
                    .take(10000)
                    .forEach { expectThat(it).isIn(range) }
            }
        }
    }

    @Test
    fun `generates both positive and negative integers over multiple runs`() {
        withCounter {
            int(-100..100).samples().take(10000).forEach { value ->
                collect(
                    when {
                        value > 0 -> "positive"
                        value < 0 -> "negative"
                        else -> "zero"
                    }
                )
            }
        }.checkPercentages(
            mapOf(
                "positive" to 45.0,
                "negative" to 45.0,
                "zero" to 0.2
            )
        )
    }

    @Test
    fun `shrinks the generated value`() {
        repeatTest { seed ->
            val random = Random(seed.value)
            val startOfRange = (-100..100).random(random)
            val range = startOfRange..(startOfRange + 50)
            val shrinkTarget = range.random(random)

            val gen = int(range = range, shrinkTarget = shrinkTarget)

            val result = gen.generate(tree(seed))
            val expectedShrinks = IntShrinker.shrink(result.value, range, shrinkTarget).toList()
            if (expectedShrinks.isEmpty()) skipIteration()
            expectThat(result).shrunkValues.isEqualTo(expectedShrinks)
        }
    }

    @Test
    fun `throws if shrink target not in range`() {
        assertThrows<IllegalArgumentException> {
            int(0..10, 20)
        }
    }

    @Test
    fun `numbers that are edge cases are produced more frequently than other numbers`() {
        val range = -10..10
        val edgeCases = setOf(-10, -9, -1, 0, 1, 9, 10)

        repeatTest { seed ->
            val subsetOfNonEdgeCases = range
                .toList()
                .shuffled(Random(seed.value))
                .minus(edgeCases)
                .take(edgeCases.size)

            val countsOfEachNumber = int(range)
                .samples(seed.value)
                .filter { it in edgeCases || it in subsetOfNonEdgeCases }
                .take(1_000)
                .groupingBy { it }
                .eachCount()

            val edgeCaseCounts = countsOfEachNumber.filterKeys { it in edgeCases }

            expectThat(edgeCaseCounts)
                .values
                .get { this.sum() }
                .describedAs { "sum: $this" }
                .get { this / countsOfEachNumber.values.sum().toDouble() }
                .describedAs { "$this of total values produced" }
                .isIn(0.47..0.61)
        }
    }

    @Test
    fun `disabling edge cases makes the distribution of numbers uniform`() {
        val range = -10..10
        val edgeCases = setOf(-10, -9, -1, 0, 1, 9, 10)

        repeatTest { seed ->
            val subsetOfNonEdgeCases = range
                .toList()
                .shuffled(Random(seed.value))
                .minus(edgeCases)
                .take(edgeCases.size)

            val countsOfEachNumber = int(range)
                .withoutDefaultEdgeCases()
                .samples(seed.value)
                .filter { it in edgeCases || it in subsetOfNonEdgeCases }
                .take(1_000)
                .groupingBy { it }
                .eachCount()

            val edgeCaseCounts = countsOfEachNumber.filterKeys { it in edgeCases }

            expectThat(edgeCaseCounts)
                .values
                .get { this.sum() }
                .describedAs { "sum: $this" }
                .get { this / countsOfEachNumber.values.sum().toDouble() }
                .describedAs { "$this of total values produced" }
                .isIn(0.43..0.56)
        }
    }
}
