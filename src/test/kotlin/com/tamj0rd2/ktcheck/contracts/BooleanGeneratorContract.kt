package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.Counter.Companion.withCounter
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.shrinkers.BoolShrinker.defaultOrigin
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.isEqualTo

internal interface BooleanGeneratorContract : BaseContract {
    @Test
    fun `generates a reasonable distribution of values over multiple runs`() {
        withCounter {
            bool()
                .samples()
                .take(100_000)
                .forEach { collect(it) }
        }.checkPercentages(mapOf(true to 49.0, false to 49.0))
    }

    @Test
    fun `using the same seed generates the same value`() {
        val gen = bool()
        val tree = ProducerTree.new()
        val values = List(1000) { gen.generate(tree).value }
        val firstValue = values.first()
        expectThat(values.drop(1)).all { isEqualTo(firstValue) }
    }

    @TestFactory
    fun `shrinks correctly`(): List<DynamicTest> {
        data class TestCase(
            val value: Boolean,
            val origin: Boolean,
            val expectedShrinks: List<Boolean>,
        )

        val testCases = listOf(
            // when value is true
            TestCase(value = true, origin = defaultOrigin(), expectedShrinks = listOf(false)),
            TestCase(value = true, origin = false, expectedShrinks = listOf(false)),
            TestCase(value = true, origin = true, expectedShrinks = emptyList()),

            // when value is false
            TestCase(value = false, origin = defaultOrigin(), expectedShrinks = emptyList()),
            TestCase(value = false, origin = false, expectedShrinks = emptyList()),
            TestCase(value = false, origin = true, expectedShrinks = listOf(true))
        )

        return testCases.map {
            DynamicTest.dynamicTest(it.toString()) {
                val result = bool(it.origin).generating(it.value)
                expectThat(result.shrunkValues).isEqualTo(it.expectedShrinks)
            }
        }
    }
}
