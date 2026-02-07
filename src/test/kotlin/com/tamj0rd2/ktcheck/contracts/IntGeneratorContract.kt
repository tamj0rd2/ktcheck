package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.doesNotContain
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import strikt.assertions.isLessThan
import strikt.assertions.isNotEmpty
import kotlin.math.abs

internal interface IntGeneratorContract : BaseContract {
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
    fun `using the same seed generates the same values`() {
        val seed = 12345L
        val gen = int(-1000..1000)
        val firstRun = gen.samples(seed).take(100).toList()
        val secondRun = gen.samples(seed).take(100).toList()
        expectThat(secondRun).isEqualTo(firstRun)
    }

    @Test
    fun `shrinks the generated value`() {
        val result = int(0..4).generating(4)
        expectThat(result).shrunkValues
        expectThat(result).shrunkValues.isEqualTo(listOf(0, 2, 3))
    }

    @Test
    fun `10 shrinks correctly`() {
        val result = int(0..10).generating(10)
        expectThat(result).shrunkValues.isEqualTo(listOf(0, 5, 8, 9))
    }

    @Test
    fun `-10 shrinks correctly`() {
        val result = int(-10..0).generating(-10)
        expectThat(result).shrunkValues.isEqualTo(listOf(0, -5, -8, -9))
    }

    @TestFactory
    fun `shrinking produces no shrinks when the original value is the shrink target`(): List<DynamicTest> {
        val range = -50..50
        val shrinkTargets = listOf(0, -25, 25, range.first, range.last)

        return shrinkTargets.map { shrinkTarget ->
            dynamicTest("shrink target: $shrinkTarget") {
                val result = int(range, shrinkTarget).generating(shrinkTarget)
                expectThat(result).shrunkValues.isEmpty()
            }
        }
    }

    @Test
    fun `shrinks for non-zero numbers always include 0`() {
        val range = -50..50
        generateSequence { range.random() }.take(100).filter { it != 0 }.forEach { value ->
            val result = int(range).generating(value)
            expectThat(result).shrunkValues.isNotEmpty().contains(0)
        }
    }

    @Test
    fun `the original generated number is not included in shrinks`() {
        val range = -50..50
        generateSequence { range.random() }.take(100).forEach { value ->
            val result = int(range).generating(value)
            expectThat(result).shrunkValues.doesNotContain(value)
        }
    }

    @Test
    fun `when 0 is in range, shrinks are closer to 0 than the original generated number`() {
        val range = -50..50
        generateSequence { range.random() }.take(100).filter { it != 0 }.forEach { value ->
            val result = int(range).generating(value)
            expectThat(result).shrunkValues
                .isNotEmpty()
                .doesNotContain(value)
                .all {
                    get { abs(this) }.describedAs("shrunk distance from 0").isLessThan(abs(value))
                }
        }
    }

    @Test
    fun `shrinks with custom shrink target in positive range`() {
        val result = int(0..10, 5).generating(10)
        expectThat(result).shrunkValues.isEqualTo(listOf(5, 8, 9))
    }

    @Test
    fun `shrinks with custom shrink target in negative range`() {
        val result = int(-20..-10, -15).generating(-10)
        expectThat(result).shrunkValues.isEqualTo(listOf(-15, -12, -11))
    }

    @Test
    fun `shrinks with custom shrink target in mixed range`() {
        val result = int(-5..10, 3).generating(7)
        expectThat(result).shrunkValues.isEqualTo(listOf(3, 5, 6))
    }

    @Test
    fun `throws if shrink target not in range`() {
        assertThrows<IllegalArgumentException> {
            int(0..10, 20)
        }
    }
}
