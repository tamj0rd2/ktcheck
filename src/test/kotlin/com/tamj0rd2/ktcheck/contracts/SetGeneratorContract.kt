package com.tamj0rd2.ktcheck.contracts

import com.tamj0rd2.ktcheck.GenerationException.DistinctCollectionSizeImpossible
import com.tamj0rd2.ktcheck.TestConfig
import com.tamj0rd2.ktcheck.checkAll
import com.tamj0rd2.ktcheck.core.ProducerTree
import com.tamj0rd2.ktcheck.core.ProducerTreeDsl.Companion.tree
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.size

internal interface SetGeneratorContract : BaseContract {
    @Test
    fun `can generate a long set without stack overflow`() {
        int().set(10_000).sample()
    }

    @Test
    fun `generates sets with distinct elements`() = checkAll(
        TestConfig().withIterations(100),
        int(0..100).set(5),
    ) { set ->
        expectThat(set.size).isEqualTo(5)
        expectThat(set).hasSize(5) // confirms no duplicates
    }

    @Test
    fun `throws when unable to generate enough distinct elements`() {
        val gen = int(0..10).set(100)
        assertThrows<DistinctCollectionSizeImpossible> { gen.sample() }
    }

    @Test
    fun `shrinks a set of 1 element`() {
        val intGen = int(0..10)
        val gen = intGen.set()

        val tree = tree {
            left(ProducerTree.new().withValue(1))
            right {
                left(intGen.findTreeProducing(4))
            }
        }

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo(setOf(4))

        expectThat(result.shrunkValues).isEqualTo(
            listOf(
                // shrinks the size
                emptySet(),
                // shrinks the value
                setOf(0),
                setOf(2),
                setOf(3),
            )
        )
    }

    @Test
    fun `shrinks a set of 2 elements`() {
        val gen = int(0..10).set()

        val result = gen.generating(setOf(1, 4))
        expectThat(result.value).isEqualTo(setOf(1, 4))

        expectThat(result.shrunkValues).containsExactlyInAnyOrder(
            // tries reducing set size (now 0)
            emptySet(),
            // continues reducing set size (now 1). From tail first, then head.
            setOf(1),
            setOf(4),
            // shrinks values, starting with index 0
            setOf(0, 4),
            // continues shrinking values at index 1
            setOf(1, 0),
            setOf(1, 2),
            setOf(1, 3),
        )
    }

    @Test
    fun `shrinks a set of 3 elements`() {
        val intGen = int(0..10)
        val gen = intGen.set()

        val tree = tree {
            // todo: fix this next!!! this is wrong. puts me back in the same annoying situation as before.
            left(ProducerTree.new().withValue(3))

            right {
                left(intGen.findTreeProducing(1))
                right {
                    left(intGen.findTreeProducing(2))
                    right {
                        left(intGen.findTreeProducing(3))
                    }
                }
            }
        }

        val result = gen.generate(tree)
        expectThat(result.value).isEqualTo(setOf(1, 2, 3))

        expectThat(result.shrunkValues).containsExactlyInAnyOrder(
            // reduce set size (0)
            emptySet(),
            // reduce set size (2), removing items at tail
            setOf(1, 2),
            // reduce set size (2), removing items at head
            setOf(2, 3),
            // shrink values
            setOf(0, 2, 3),
            setOf(1, 0, 3),
            // next would try (1,1,3) but encounters duplicate 1, stops rather than generating further values
            setOf(1, 3),
            setOf(1, 2, 0),
            // next would try (1,2,2) but encounters duplicate 2, stops rather than generating further values
            setOf(1, 2),
        )
    }

    @Test
    fun `can shrink sets with a minimum size greater than 0`() {
        int().set(1..2).expectGenerationAndShrinkingToEventuallyComplete()
        int().set(2..2).expectGenerationAndShrinkingToEventuallyComplete()
        int().set(2..5).expectGenerationAndShrinkingToEventuallyComplete()
    }

    @Test
    fun `all generated sets contain only distinct elements`() = checkAll(
        TestConfig().withIterations(100),
        int(0..100).set(0..10),
    ) { set ->
        // If we convert to a set again, the size should remain the same (proving all elements were distinct)
        expectThat(set.toSet()).hasSize(set.size)
    }

    @Test
    fun `does not produce any shrinks when the set size is equal to the number of distinct values`() {
        // note: there are only 3 possible distinct values. So a set of size 3 can only ever be achieved once: (0, 1, 2)
        val intGen = int(0..2)
        val gen = intGen.set(3)

        val result = gen.generate(ProducerTree.new())
        expectThat(result.value).size.isEqualTo(3)
        expectThat(result.shrunkValues).isEmpty()
    }
}
