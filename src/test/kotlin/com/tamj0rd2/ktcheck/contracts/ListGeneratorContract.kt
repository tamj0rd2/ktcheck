package com.tamj0rd2.ktcheck.contracts

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
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isIn
import strikt.assertions.isLessThan
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.none
import strikt.assertions.size

internal interface ListGeneratorContract : BaseContract {
    // todo: remove constraint. the full list size causes a timeout.
    override val exampleGen get() = int().list(0..10)

    @Test
    fun `can generate a long list without stack overflow`() {
        constant(1).list(10_000).sample()
    }

    @Test
    fun `shrinks a list of 1 element`() {
        val range = 0..10
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

        fun checkShrinks(result: GenResults<List<Int>>) {
            if (result.value.size < 2 || result.value.size % 2 != 0) skipIteration()
            val halfSize = result.value.size / 2

            expectThat(result).shrunkValues.isNotEmpty().contains(
                result.value.take(halfSize),
                result.value.takeLast(halfSize),
            )
        }

        repeatTest { seed -> checkShrinks(gen.generate(tree(seed))) }

        // todo: if I decide to keep current, remove condition.
        if (this !is com.tamj0rd2.ktcheck.current.BaseContractImpl) {
            gen.edgeCases().forEach { ignoreSkips { checkShrinks(it) } }
        }
    }

    @Test
    fun `shrinks to empty list when list is not empty`() {
        val gen = int(0..10).list()

        fun checkShrinks(result: GenResults<List<Int>>) {
            if (result.value.isEmpty()) skipIteration()
            expectThat(result).shrunkValues.isNotEmpty().first().isEqualTo(emptyList())
        }

        repeatTest { seed -> checkShrinks(gen.generate(tree(seed))) }

        // todo: if I decide to keep current, remove condition.
        if (this !is com.tamj0rd2.ktcheck.current.BaseContractImpl) {
            gen.edgeCases().forEach { ignoreSkips { checkShrinks(it) } }
        }
    }

    @Test
    fun `shrunk element values do not exceed max original value`() {
        val gen = int(0..10).list(size = 0..4)
        fun checkShrinks(result: GenResults<List<Int>>) {
            if (result.value.isEmpty()) skipIteration()
            val maxOriginalValue = result.value.max()

            expectThat(result).shrunkValues.isNotEmpty().all {
                all { isLessThanOrEqualTo(maxOriginalValue) }
            }
        }

        repeatTest { seed -> checkShrinks(gen.generate(tree(seed))) }

        // todo: if I decide to keep current, remove condition.
        if (this !is com.tamj0rd2.ktcheck.current.BaseContractImpl) {
            gen.edgeCases().forEach { ignoreSkips { checkShrinks(it) } }
        }
    }

    @Test
    fun `all shrunk element values are within the generator range`() {
        val range = 0..10
        val gen = int(range).list()

        fun checkShrinks(result: GenResults<List<Int>>) {
            if (result.value.isEmpty()) skipIteration()
            expectThat(result).shrunkValues.isNotEmpty().all { all { isIn(range) } }
        }

        repeatTest { seed -> checkShrinks(gen.generate(tree(seed))) }

        // todo: if I decide to keep current, remove condition.
        if (this !is com.tamj0rd2.ktcheck.current.BaseContractImpl) {
            gen.edgeCases().forEach { ignoreSkips { checkShrinks(it) } }
        }
    }

    @TestFactory
    fun `edge case generation`(): List<DynamicTest> {
        data class TestCase(
            val sizeRange: IntRange,
            val shouldHaveEmpty: Boolean,
            val shouldHaveSingleton: Boolean,
            val shouldHaveDuplicates: Boolean,
        ) {
            val description = "for a list with sizeRange $sizeRange"
        }

        val testCases = listOf(
            TestCase(
                sizeRange = 0..10,
                shouldHaveEmpty = true,
                shouldHaveSingleton = true,
                shouldHaveDuplicates = true,
            ),
            TestCase(
                sizeRange = 1..5,
                shouldHaveEmpty = false,
                shouldHaveSingleton = true,
                shouldHaveDuplicates = true,
            ),
            TestCase(
                sizeRange = 3..10,
                shouldHaveEmpty = false,
                shouldHaveSingleton = false,
                shouldHaveDuplicates = true,
            ),
            TestCase(
                sizeRange = 0..0,
                shouldHaveEmpty = true,
                shouldHaveSingleton = false,
                shouldHaveDuplicates = false,
            ),
            TestCase(
                sizeRange = 2..2,
                shouldHaveEmpty = false,
                shouldHaveSingleton = false,
                shouldHaveDuplicates = true,
            ),
            TestCase(
                sizeRange = 1..1,
                shouldHaveEmpty = false,
                shouldHaveSingleton = true,
                shouldHaveDuplicates = false,
            )
        )

        return testCases.map { tc ->
            dynamicTest(tc.description) {
                val gen = int(0..10).list(tc.sizeRange)
                val edgeCaseValues = gen.edgeCases().map { it.value }.toList()

                when (tc.shouldHaveEmpty) {
                    true -> expectThat(edgeCaseValues).any { isEmpty() }
                    false -> expectThat(edgeCaseValues).none { isEmpty() }
                }

                when (tc.shouldHaveSingleton) {
                    true -> expectThat(edgeCaseValues).any { size.isEqualTo(1) }
                    false -> expectThat(edgeCaseValues).none { size.isEqualTo(1) }
                }

                when (tc.shouldHaveDuplicates) {
                    true -> expectThat(edgeCaseValues).any { size.isGreaterThan(subject.toSet().size) }
                    false -> expectThat(edgeCaseValues).none { size.isGreaterThan(subject.toSet().size) }
                }
            }
        }
    }
}
