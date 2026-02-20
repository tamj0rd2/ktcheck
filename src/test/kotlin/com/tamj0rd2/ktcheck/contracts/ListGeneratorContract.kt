package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.all
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.any
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import strikt.assertions.isLessThan
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.size

internal interface ListGeneratorContract : BaseContract {
    @Test
    fun `can generate a long list without stack overflow`() {
        constant(1).list(10_000).sample()
    }

    @Test
    fun `shrinks a list of 1 element`() {
        val range = IntRange.all
        // todo: it'd be kind of convenient if IntGen had a shrink method on it that took an int. Then I wouldn't
        //  need to duplicate the range and shrinkingTarget in so many tests.
        val gen = int(range, 0).list(0..5)

        repeatTest { seed ->
            val result = gen.generate(tree(seed))
            if (result.value.size != 1) skipIteration()

            val expectedValueShrinks = IntShrinker.shrink(result.value.single(), range, 0).toList()

            expectThat(result).shrunkValues.containsExactlyInAnyOrder(
                emptyList(),
                *expectedValueShrinks.map { listOf(it) }.toTypedArray()
            )
        }
    }

    @Test
    fun `recursively shrinks a list of 2 elements`() {
        val gen = int(0..4).list()

        val result = gen.generating(listOf(3, 4))

        expectThat(result).shrunkValues.isNotEmpty().contains(
            // size shrinks
            emptyList(),
            listOf(3),
            listOf(4),
            // element shrinks
            listOf(0, 4),
            listOf(3, 0),
        )
    }

    @Test
    fun `once the elements of a list have been shrunk, the resultant shrinks can also be shrunk by size`() {
        val gen = int(0..10).list(0..3)

        val root = gen.generating(listOf(3, 4))

        val firstNonSizeShrink = root.shrinks.first { it.value.size == root.value.size }
        expectThat(firstNonSizeShrink).shrunkValues.any { size.isLessThan(root.value.size) }
    }

    @Test
    fun `size shrinks include the first half of the list, and the second half of the list`() {
        val gen = int().list(0..20)

        repeatTest { seed ->
            val result = gen.generate(tree(seed))

            if (result.value.size < 2 || result.value.size % 2 != 0) skipIteration()
            val halfSize = result.value.size / 2

            expectThat(result).shrunkValues.contains(
                result.value.take(halfSize),
                result.value.takeLast(halfSize),
            )
        }
    }

    @Test
    fun `shrinks to empty list when list is not empty`() {
        repeatTest { seed ->
            val gen = int(0..10).list()
            val result = gen.generate(tree(seed))
            if (result.value.isEmpty()) skipIteration()
            expectThat(result).shrunkValues.first().isEqualTo(emptyList())
        }
    }

    @Test
    fun `shrunk element values do not exceed max original value`() {
        repeatTest { seed ->
            val gen = int(0..10).list(size = 0..4)

            val result = gen.generate(tree(seed))
            if (result.value.isEmpty()) skipIteration()
            val maxOriginalValue = result.value.max()

            expectThat(result).shrunkValues.all {
                all { isLessThanOrEqualTo(maxOriginalValue) }
            }
        }
    }

    @Test
    fun `all shrunk element values are within the generator range`() {
        repeatTest { seed ->
            val range = 0..10
            val gen = int(range).list()

            val result = gen.generate(tree(seed))
            if (result.value.isEmpty()) skipIteration()
            expectThat(result).shrunkValues.all { all { isIn(range) } }
        }
    }

    @TestFactory
    fun `edge case generation`(): List<DynamicTest> {
        data class TestCase(
            val sizeRange: IntRange,
            val expectedInclusions: Set<List<Int>>,
            val expectedExclusions: Set<List<Int>>,
        ) {
            val description = "for a list with sizeRange $sizeRange"
        }

        val testCases = listOf(
            TestCase(
                sizeRange = 0..10,
                expectedInclusions = setOf(emptyList(), listOf(0), listOf(10), listOf(0, 0), listOf(10, 10)),
                expectedExclusions = emptySet()
            ),
            TestCase(
                sizeRange = 1..5,
                expectedInclusions = setOf(listOf(0), listOf(10), listOf(0, 0), listOf(10, 10)),
                expectedExclusions = setOf(emptyList())
            ),
            TestCase(
                sizeRange = 3..10,
                expectedInclusions = setOf(listOf(0, 0, 0), listOf(10, 10, 10)),
                expectedExclusions = setOf(emptyList(), listOf(0), listOf(10), listOf(0, 0), listOf(10, 10))
            ),
            TestCase(
                sizeRange = 5..10,
                expectedInclusions = setOf(listOf(0, 0, 0, 0, 0), listOf(10, 10, 10, 10, 10)),
                expectedExclusions = setOf(emptyList(), listOf(0), listOf(10), listOf(0, 0), listOf(10, 10))
            ),
            TestCase(
                sizeRange = 0..0,
                expectedInclusions = setOf(emptyList()),
                expectedExclusions = setOf(listOf(0), listOf(10), listOf(0, 0), listOf(10, 10))
            ),
            TestCase(
                sizeRange = 2..2,
                expectedInclusions = setOf(listOf(0, 0), listOf(10, 10)),
                expectedExclusions = setOf(emptyList(), listOf(0), listOf(10))
            ),
            TestCase(
                sizeRange = 1..1,
                expectedInclusions = setOf(listOf(0), listOf(10)),
                expectedExclusions = setOf(emptyList(), listOf(0, 0), listOf(10, 10))
            )
        )

        return testCases.map { tc ->
            dynamicTest(tc.description) {
                val gen = int(0..10).list(tc.sizeRange)
                val edgeCaseValues = gen.edgeCases().map { it.value }.toList()

                if (tc.expectedInclusions.isNotEmpty()) {
                    expectThat(edgeCaseValues).contains(tc.expectedInclusions)
                }

                if (tc.expectedExclusions.isNotEmpty()) {
                    expectThat(edgeCaseValues).not().contains(tc.expectedExclusions)
                }
            }
        }
    }
}
