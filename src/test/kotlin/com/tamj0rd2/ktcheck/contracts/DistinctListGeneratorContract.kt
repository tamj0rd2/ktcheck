package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.GenerationException.DistinctCollectionSizeImpossible
import com.tamj0rd2.ktcheck.core.shrinkers.IntShrinker.shrink
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertTimeoutPreemptively
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.size
import java.time.Duration

internal interface DistinctListGeneratorContract : BaseContract {
    override val exampleGen get() = int(0..10).distinctList(0..4)

    @Test
    fun `can generate a long distinct list without stack overflow`() {
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            int().distinctList(10_000).sample()
        }
    }

    @Test
    fun `generates lists with distinct elements`() {
        val gen = int(0..100).distinctList(5)

        repeatTest { seed ->
            val result = gen.generate(tree(seed))
            expectThat(result).value.hasSize(5)
            expectThat(result).value.get { toSet() }.hasSize(5)
        }
    }

    @Test
    fun `throws when unable to generate enough distinct elements`() {
        val gen = int(0..10).distinctList(100)
        assertThrows<DistinctCollectionSizeImpossible> { gen.sample() }
    }

    @Test
    fun `shrinks a list of 1 element`() {
        repeatTest { seed ->
            val gen = int(0..10).distinctList(size = 0..5)
            val result = gen.generate(tree(seed))
            if (result.value.size != 1) skipIteration()

            val expectedValueShrinks = shrink(result.value.single(), 0..10).map { listOf(it) }.toList()
            expectThat(result).shrunkValues.first().isEqualTo(emptyList())
            expectThat(result).shrunkValues.get { drop(1) }.isEqualTo(expectedValueShrinks)
        }
    }

    @Test
    fun `shrinks a list of 2 elements`() {
        repeatTest { seed ->
            val gen = int(0..10).distinctList(size = 0..10)
            val result = gen.generate(tree(seed))
            if (result.value.size != 2) skipIteration()

            val firstValue = result.value[0]
            val secondValue = result.value[1]

            val firstValueShrunk = shrink(firstValue, 0..10).map { listOf(it, secondValue) }
            val secondValueShrunk = shrink(secondValue, 0..10).map { listOf(firstValue, it) }
            val expectedValueShrinks = (firstValueShrunk + secondValueShrunk).filter { it.toSet().size == 2 }.toList()

            expectThat(result).shrunkValues.isNotEmpty().containsExactlyInAnyOrder(
                // tries reducing set size (now 0)
                listOf(),
                // continues reducing set size (now 1). From tail first, then head.
                listOf(firstValue),
                listOf(secondValue),
                // element shrinks
                *expectedValueShrinks.toTypedArray(),
            )
        }
    }

    @Test
    fun `can shrink lists with a minimum size greater than 0`() {
        int().distinctList(1..2).expectGenerationAndShrinkingToEventuallyComplete()
        int().distinctList(2..2).expectGenerationAndShrinkingToEventuallyComplete()
        int().distinctList(2..5).expectGenerationAndShrinkingToEventuallyComplete()
    }

    @Test
    fun `all generated lists contain only distinct elements`() {
        repeatTest { seed ->
            val gen = int(0..10).distinctList(0..10)
            val result = gen.generate(tree(seed))
            expectThat(result.value.toSet()).hasSize(result.value.size)
        }
    }

    @Test
    fun `does not produce any shrinks when the list size is equal to the number of distinct values`() {
        // note: there are only 3 possible distinct values. So a distinct list of size 3 can only ever be achieved once
        val intGen = int(1..3)
        val gen = intGen.distinctList(3)

        val result = gen.generate(tree())
        expectThat(result).value.hasSize(3)
        expectThat(result).shrunkValues.isEmpty()
    }

    @Test
    fun `shrinks to empty list when list is not empty`() {
        val gen = int(0..10).distinctList()

        repeatTest { seed ->
            val result = gen.generate(tree(seed))
            if (result.value.isEmpty()) skipIteration()
            expectThat(result).shrunkValues.first().isEqualTo(emptyList())
        }
    }

    @Test
    fun `no shrunk element values exceed the max original value`() {
        repeatTest { seed ->
            val gen = int(0..10).distinctList(0..4)

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
            val gen = int(range).distinctList()

            val result = gen.generate(tree(seed))
            if (result.value.isEmpty()) skipIteration()
            expectThat(result).shrunkValues.all { all { isIn(range) } }
        }
    }

    @Test
    fun `all shrunk lists fall within the specified size bounds`() {
        repeatTest { seed ->
            val minSize = 2
            val gen = int(0..10).distinctList(minSize..10)

            val result = gen.generate(tree(seed))
            val originalSize = result.value.size
            expectThat(result).shrunkValues.all { size.isIn(minSize..originalSize) }
        }
    }
}
