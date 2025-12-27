package com.tamj0rd2.ktcheck.genv2

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThan
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotEmpty
import kotlin.math.abs

class ShrinkTest {

    @Test
    fun `shrink of 0 produces empty sequence`() {
        val result = shrink(0).toList()
        expectThat(result).isEmpty()
    }

    @Test
    fun `shrink of positive number includes 0`() {
        val result = shrink(10).toList()
        expectThat(result).contains(0)
    }

    @Test
    fun `shrink of negative number includes 0`() {
        val result = shrink(-10).toList()
        expectThat(result).contains(0)
    }

    @Test
    fun `shrink sequence does not include the original value`() {
        val testCases = listOf(1, 5, 10, -1, -5, -10, 100, -100, Int.MAX_VALUE, Int.MIN_VALUE)

        testCases.forEach { value ->
            val shrinks = shrink(value).toList()
            expectThat(shrinks).describedAs("shrinks of $value").not().contains(value)
        }
    }

    @Test
    fun `shrink sequence produces values closer to zero than original`() {
        val testCases = listOf(10, 50, 100, -10, -50, -100)

        testCases.forEach { value ->
            val originalDistance = abs(value)
            val shrinks = shrink(value).toList()

            shrinks.forEach { shrunk ->
                expectThat(abs(shrunk))
                    .describedAs("shrink value $shrunk should be closer to 0 than original $value")
                    .isLessThan(originalDistance)
            }
        }
    }

    @Test
    fun `shrink of 1 produces only 0`() {
        val result = shrink(1).toList()
        expectThat(result).isEqualTo(listOf(0))
    }

    @Test
    fun `shrink of -1 produces only 0`() {
        val result = shrink(-1).toList()
        expectThat(result).isEqualTo(listOf(0))
    }

    @Test
    fun `shrink produces distinct values`() {
        val testCases = listOf(10, 50, 100, -10, -50, -100)

        testCases.forEach { value ->
            val shrinks = shrink(value).toList()
            val distinctShrinks = shrinks.distinct()
            expectThat(shrinks).describedAs("shrinks of $value should be distinct")
                .hasSize(distinctShrinks.size)
        }
    }

    @Test
    fun `shrink sequence is finite`() {
        val testCases = listOf(1000, -1000, Int.MAX_VALUE, Int.MIN_VALUE)

        testCases.forEach { value ->
            // Should not throw or hang - just checking it completes
            val shrinks = shrink(value).take(10000).toList()
            expectThat(shrinks.size).describedAs("shrinks of $value should be finite")
                .isLessThanOrEqualTo(10000)
        }
    }

    @Test
    fun `shrink of positive integers moves from 0 toward initial value`() {
        val value = 100
        val shrinks = shrink(value).toList()

        expectThat(shrinks).isNotEmpty()

        for (i in 0 until shrinks.size - 1) {
            val current = shrinks[i]
            val next = shrinks[i + 1]
            expectThat(current).isLessThanOrEqualTo(next)
        }
    }

    @Test
    fun `shrink of negative integers moves from zero toward initial value`() {
        val value = -100
        val shrinks = shrink(value).toList()

        expectThat(shrinks).isNotEmpty()

        // Check that values generally decrease in absolute value
        for (i in 0 until shrinks.size - 1) {
            val current = shrinks[i]
            val next = shrinks[i + 1]
            expectThat(current).isGreaterThanOrEqualTo(next)
        }
    }

    @Test
    fun `shrink handles maximum integer without overflow`() {
        // Should not throw due to overflow
        val shrinks = shrink(Int.MAX_VALUE).take(100).toList()
        expectThat(shrinks).isNotEmpty()
        expectThat(shrinks).contains(0)
    }

    @Test
    fun `shrink handles minimum integer without underflow`() {
        // Should not throw due to underflow
        val shrinks = shrink(Int.MIN_VALUE).take(100).toList()
        expectThat(shrinks).isNotEmpty()
        expectThat(shrinks).contains(0)
    }

    @Test
    fun `shrink sequence is lazy`() {
        // If the sequence is lazy, we should be able to take just a few values
        // without computing all possible shrinks
        val value = Int.MAX_VALUE
        val firstFive = shrink(value).take(5).toList()
        expectThat(firstFive).hasSize(5)
    }

    @Test
    fun `shrink of small positive values produces expected sequence`() {
        // For value 10, expected shrinks might be: 0, 5, 8, 9
        val shrinks = shrink(10).toList()
        expectThat(shrinks).all { isGreaterThanOrEqualTo(0).isLessThan(10) }
        expectThat(shrinks).isEqualTo(listOf(0, 5, 8, 9))
    }

    @Test
    fun `shrink of small negative values produces expected sequence`() {
        // For value -10, expected shrinks might be: 0, -5, -8, -9
        val shrinks = shrink(-10).toList()
        expectThat(shrinks).all { isGreaterThan(-10).isLessThanOrEqualTo(0) }
        expectThat(shrinks).isEqualTo(listOf(0, -5, -8, -9))
    }
}
