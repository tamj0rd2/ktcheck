package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isIn

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
            DynamicTest.dynamicTest(desc) {
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
    fun `recursively shrinks depth first`() {
        val result = int(0..4).generating(4)

        val shrunkValues = result.deeplyShrunkValues.toList().distinct()
        expectThat(shrunkValues).isEqualTo(listOf(0, 2, 1, 3))
    }
}
